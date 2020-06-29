package org.encryfoundation.tg

import cats.Applicative
import cats.data.OptionT
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync, Timer}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger

import collection.JavaConverters._
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.drinkless.tdlib.TdApi.{MessagePhoto, MessageText, MessageVideo}
import org.drinkless.tdlib.{Client, ResultHandler, TdApi}
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.handlers.{EmptyHandler, SecretChatCreationHandler}
import org.encryfoundation.tg.javaIntegration.AuthMsg.{LoadChatsWindow, LoadPassWindow, LoadVCWindow}
import org.encryfoundation.tg.pipelines.Pipelines
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializer
import org.encryfoundation.tg.services.{PrivateConferenceService, UserStateService}
import org.encryfoundation.tg.userState.UserState
import org.encryfoundation.tg.utils.UserStateUtils
import scorex.crypto.encode.Base64

import scala.io.StdIn
import scala.util.{Failure, Success}

case class Handler[F[_]: ConcurrentEffect: Timer: Logger](userStateRef: Ref[F, UserState[F]],
                                                          privateConferenceService: PrivateConferenceService[F],
                                                          userStateService: UserStateService[F],
                                                          client: Client[F]) extends ResultHandler[F] {

  /**
   * Callback called on result of query to TDLib or incoming update from TDLib.
   *
   * @param obj Result of query or update of type TdApi.Update about new events.
   */

  override def onResult(obj: TdApi.Object): F[Unit] = obj match {
      case authEvent: TdApi.UpdateAuthorizationState =>
        authHandler(authEvent, client)
      case updateNewChat: TdApi.UpdateNewChat =>
        for {
          _ <- Logger[F].info(s"Receive update new chat with chat id: ${updateNewChat.chat}")
          (chatWithOrder) <- Sync[F].delay {
            val chat: TdApi.Chat = updateNewChat.chat
            val newOrder = chat.order
            chat.order = 0
            (chat, newOrder)
          }
          _ <- userStateService.addChat(updateNewChat.chat)
          _ <- userStateService.updateChatOrder(chatWithOrder._1, chatWithOrder._2)
        } yield ()
        //userStateService.addChat(updateNewChat.chat) >> userStateService.updateChatOrder(updateNewChat.chat, updateNewChat.chat.order)
      case updateUser: TdApi.UpdateUser => userStateService.updateUser(updateUser.user)
      case updateChatOrder: TdApi.UpdateChatOrder =>
        OptionT(userStateService.getChatById(updateChatOrder.chatId)).flatMap { chat =>
          OptionT.liftF(Logger[F].info(s"Update chat order for ${chat}")).flatMap { _ =>
            OptionT.liftF(userStateService.updateChatOrder(chat, updateChatOrder.order))
          }
        }.fold(())(_ => ())
      case updateChat: TdApi.UpdateChatLastMessage =>
        OptionT(userStateService.getChatById(updateChat.chatId)).flatMap { chat =>
          OptionT.liftF(Sync[F].delay(chat.lastMessage = updateChat.lastMessage) >>
            userStateService.updateChatOrder(chat, updateChat.order))
        }.fold(())(_ => ())
      case basicGroup: TdApi.UpdateBasicGroup =>
        userStateService.updateBasicGroup(basicGroup)
      case superGroup: TdApi.UpdateSupergroup =>
        userStateService.updateSuperGroup(superGroup)
      case secretChat: TdApi.UpdateSecretChat =>
        secretChat.secretChat.state match {
          case _: TdApi.SecretChatStateClosed => userStateService.removeSecretChat(secretChat.secretChat)
          case _: TdApi.SecretChatStatePending if !secretChat.secretChat.isOutbound =>
            for {
              _ <- Logger[F].info(s"Accept secret chat: ${secretChat}")
              _ <- client.send(new TdApi.OpenChat(secretChat.secretChat.id), SecretChatCreationHandler[F](userStateRef))
              _ <- userStateService.addSecretChat(secretChat.secretChat)
            } yield ()
          case _: TdApi.SecretChatStateReady =>
            Logger[F].info(s"Secret chat ready: ${secretChat}") >>
            userStateService.getPipelineChatIdBySecChat(secretChat.secretChat.id).flatMap {
              case Some(chatId) =>
                for {
                  possiblePipeline <- userStateService.getPipeline(chatId)
                  _ <- Logger[F].info(s"pipeline: ${possiblePipeline}. Chatid: ${chatId}")
                  _ <- possiblePipeline match {
                    case Some(pipeline) =>
                      pipeline.processInput(Array.emptyByteArray).flatMap( newPipeline =>
                        Logger[F].info(s"Start pipeline for chat with id: ${chatId}") >>
                          userStateService.updatePipelineChat(chatId, newPipeline)
                      )
                    case None => Logger[F].info(s"No pipeline chat for secret chat ${secretChat.secretChat.id}")
                  }
                } yield ()
              case None => Logger[F].info(s"No pipeline chat for secret chat ${secretChat.secretChat.id}")
            } >> ().pure[F]
          case res => Logger[F].info(s"Receive for secret chat: ${secretChat}")
        }
      case msg: TdApi.UpdateNewMessage =>
        for {
          _ <- msg.message.content match {
            case a: MessageText =>
              Base64.decode(a.text.text) match {
                case Success(value) =>
                  StepMsgSerializer.parseBytes(value) match {
                    case Right(stepMsg) if !msg.message.isOutgoing =>
                      userStateService.getPipeline(msg.message.chatId) flatMap {
                        case Some(pipeline) => for {
                          newPipeLine <- pipeline.processInput(value)
                          _ <- userStateService.updatePipelineChat(msg.message.chatId, newPipeLine)
                        } yield ()
                        case None => Pipelines.findStart(
                          userStateRef,
                          userStateService,
                          msg.message.chatId,
                          client,
                          stepMsg
                        )
                      }
                    case Right(_) => processLastMessage(msg.message)
                    case Left(_) => processLastMessage(msg.message)
                  }
                case Failure(err) => processLastMessage(msg.message)
              }
            case _ => processLastMessage(msg.message)
          }
        } yield ()
      case _ => Logger[F].info(s"Receive unkown elem3: ${obj}")
    }

  def authHandler(authEvent: TdApi.UpdateAuthorizationState, client: Client[F]): F[Unit] = {
    authEvent.authorizationState match {
      case a: TdApi.AuthorizationStateWaitTdlibParameters =>
        val parameters = new TdApi.TdlibParameters
        parameters.databaseDirectory = "tdlib"
        parameters.useMessageDatabase = true
        parameters.useSecretChats = true
        parameters.apiId = 1257765
        parameters.apiHash = "8f6d710676dd9cb77c6c7fe24f09ee15"
        parameters.systemLanguageCode = "en"
        parameters.deviceModel = "Desktop"
        parameters.systemVersion = "Unknown"
        parameters.applicationVersion = "0.1"
        parameters.enableStorageOptimizer = true
        Logger[F].info("Setting td-lib settings") >> client.send(
          new TdApi.SetTdlibParameters(parameters), AuthRequestHandler[F]()
        )
      case a: TdApi.AuthorizationStateWaitEncryptionKey =>
        client.send(new TdApi.CheckDatabaseEncryptionKey(), AuthRequestHandler[F]())
      case a: TdApi.AuthorizationStateWaitPhoneNumber =>
        for {
          phoneNumber <- UserStateUtils.getPhoneNumber(userStateRef)
          _ <- client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), AuthRequestHandler())
        } yield ()
      case a: TdApi.AuthorizationStateWaitCode =>
        for {
          state <- userStateRef.get
          _ <- Sync[F].delay(state.javaState.get().authQueue.put(LoadVCWindow))
          code <- UserStateUtils.getVC(userStateRef)
          _ <- client.send(new TdApi.CheckAuthenticationCode(code), AuthRequestHandler())
        } yield ()
      case a: TdApi.AuthorizationStateWaitPassword =>
        for {
          state <- userStateRef.get
          _ <- Sync[F].delay(state.javaState.get().authQueue.put(LoadPassWindow))
          pass <- UserStateUtils.getPass(userStateRef)
          _ <- client.send(new TdApi.CheckAuthenticationPassword(pass), AuthRequestHandler())
        } yield ()
      case a: TdApi.AuthorizationStateReady =>
        for {
          _ <- userStateService.setAuth()
          state <- userStateRef.get
          _ <- Sync[F].delay(state.javaState.get().authQueue.put(LoadChatsWindow))
          _ <- client.send(
            new TdApi.GetChats(new TdApi.ChatListMain(), Long.MaxValue, 0, 20),
            EmptyHandler[F]()
          )
        } yield ()
      case _ =>
        println(s"Got unknown event in auth. ${authEvent}").pure[F]
    }
  }

  def processLastMessage(msg: TdApi.Message): F[Unit] = {

    def msg2Str(msg: TdApi.Message, state: UserState[F]): String =
      msg.content match {
        case text: MessageText if state.privateGroups.exists(_.chatId == msg.chatId) =>
          val aes = AESEncryption(state.privateGroups.find(_.chatId == msg.chatId).get.password.getBytes())
          state.users(msg.senderUserId).phoneNumber + ": " + aes.decrypt(Base64.decode(text.text.text).get).map(_.toChar).mkString
        case text: MessageText => state.users(msg.senderUserId).phoneNumber + ": " + text.text.text
        case _: MessagePhoto => state.users(msg.senderUserId).phoneNumber + ": " + "photo"
        case _: MessageVideo => state.users(msg.senderUserId).phoneNumber + ": " + "video"
        case _ => state.users(msg.senderUserId).phoneNumber + ": Unknown msg type"
      }

    for {
      state <- userStateRef.get
      _ <- if (msg.chatId == state.activeChat) Sync[F].delay {
        val javaState = state.javaState.get()
        val localDialogHistory = javaState.activeDialog.getContent
        localDialogHistory.append(msg2Str(msg, state) + "\n")
        javaState.activeDialogArea.setText(localDialogHistory.toString)
        javaState.activeDialog.setContent(localDialogHistory)
      } else Applicative[F].pure(())
    } yield ()
  }
}

object Handler {
  def apply[F[_]: ConcurrentEffect: Timer: Logger](stateRef: Ref[F, UserState[F]],
                                                   queueRef: Ref[F, List[TdApi.Object]],
                                                   privateConferenceService: PrivateConferenceService[F],
                                                   userStateService: UserStateService[F],
                                                   client: Client[F]): F[Handler[F]] = {
    val handler = new Handler(stateRef, privateConferenceService, userStateService, client)
    for {
      list <- queueRef.get
      _ <- Sync[F].delay(list)
      _ <- list.foreach(elem => Sync[F].delay(println(s"Send: ${elem.getConstructor}. ${list}")) >> handler.onResult(elem)).pure[F]
      handlerExp <- handler.pure[F]
    } yield handlerExp
  }
}
