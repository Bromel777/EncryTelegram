package org.encryfoundation.tg

import cats.Applicative
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
import org.encryfoundation.tg.pipelines.Pipelines
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializer
import org.encryfoundation.tg.services.PrivateConferenceService
import org.encryfoundation.tg.userState.UserState
import org.encryfoundation.tg.utils.UserStateUtils
import scorex.crypto.encode.Base64

import scala.io.StdIn
import scala.util.{Failure, Success}

case class Handler[F[_]: ConcurrentEffect: Timer: Logger](userStateRef: Ref[F, UserState[F]],
                                                          privateConferenceService: PrivateConferenceService[F],
                                                          client: Client[F]) extends ResultHandler[F] {

  /**
   * Callback called on result of query to TDLib or incoming update from TDLib.
   *
   * @param obj Result of query or update of type TdApi.Update about new events.
   */

  override def onResult(obj: TdApi.Object): F[Unit] = {
    obj.getConstructor match {
      case TdApi.UpdateAuthorizationState.CONSTRUCTOR =>
        for {
          state <- userStateRef.get
          _ <- authHandler(obj.asInstanceOf[TdApi.UpdateAuthorizationState], state.client)
        } yield ()
      case TdApi.UpdateNewChat.CONSTRUCTOR =>
        val updateNewChat: TdApi.UpdateNewChat = obj.asInstanceOf[TdApi.UpdateNewChat]
        val chat: TdApi.Chat = updateNewChat.chat
        val newOrder = chat.order
        chat.order = 0
        for {
          state <- userStateRef.get
          _ <- Logger[F].info(s"Receive chat: ${obj}")
          _ <- userStateRef.update(_.copy(chatIds = state.chatIds + (chat.id -> chat)))
//          newState <- state.checkChat(chat)
//          _ <- userStateRef.update(_ => newState)
          _ <- setChatOrder(chat, newOrder)
        } yield ()
      case TdApi.UpdateUser.CONSTRUCTOR =>
        val updateUser = obj.asInstanceOf[TdApi.UpdateUser]
        for {
          _ <- userStateRef.update { prevState =>
            val newUsers = prevState.javaState.get().getUsersMap
            newUsers.put(updateUser.user.id, updateUser.user)
            prevState.javaState.get().setUsersMap(newUsers)
            prevState.copy(users = prevState.users + (updateUser.user.id -> updateUser.user))
          }
        } yield ()
      case TdApi.UpdateChatOrder.CONSTRUCTOR =>
        val updateChatOrder = obj.asInstanceOf[TdApi.UpdateChatOrder]
        for {
          _ <- Logger[F].info(s"Receive UpdateChatOrder: ${updateChatOrder}")
          state <- userStateRef.get
          _ <- state.chatIds.find(_._1 == updateChatOrder.chatId).traverse { case (_, chat) =>
            setChatOrder(chat, updateChatOrder.order)
          }
        } yield ()
      case TdApi.UpdateChatLastMessage.CONSTRUCTOR =>
        val updateChat = obj.asInstanceOf[TdApi.UpdateChatLastMessage]
        for {
          state <- userStateRef.get
          _ <- state.chatIds.find(_._1 == updateChat.chatId).traverse { case (_, chat) =>
            chat.lastMessage = updateChat.lastMessage
            setChatOrder(chat, updateChat.order)
          }
        } yield ()
      case TdApi.UpdateBasicGroup.CONSTRUCTOR =>
        val basicGroup = obj.asInstanceOf[TdApi.UpdateBasicGroup]
        for {
          state <- userStateRef.get
          _ <- userStateRef.update(
            _.copy(basicGroups = state.basicGroups + (basicGroup.basicGroup.id -> basicGroup.basicGroup))
          )
        } yield ()
      case TdApi.UpdateSupergroup.CONSTRUCTOR =>
        val superGroup = obj.asInstanceOf[TdApi.UpdateSupergroup]
        for {
          state <- userStateRef.get
          _ <- userStateRef.update(
            _.copy(superGroups = state.superGroups + (superGroup.supergroup.id -> superGroup.supergroup))
          )
        } yield ()
      case TdApi.UpdateSecretChat.CONSTRUCTOR =>
        val secretChat = obj.asInstanceOf[TdApi.UpdateSecretChat]
        for {
          state <- userStateRef.get
          _ <- userStateRef.update(
            _.copy(secretChats = state.secretChats + (secretChat.secretChat.id -> secretChat.secretChat))
          )
          _ <- if (
            secretChat.secretChat.state.isInstanceOf[TdApi.SecretChatStateReady] &&
              state.pendingSecretChatsForInvite.contains(secretChat.secretChat.id)
          ) for {
              _ <- Logger[F].info("Secret chat for key sharing accepted. Start first step!")
              chatInfo <- state.pendingSecretChatsForInvite(secretChat.secretChat.id).pure[F]
              chatId <- state.pendingSecretChatsForInvite(secretChat.secretChat.id)._1.id.pure[F]
              newPipeline <- state.pipelineSecretChats(chatId).processInput(Array.emptyByteArray)
              _ <- userStateRef.update { prevState =>
                prevState.copy(
                  pendingSecretChatsForInvite = state.pendingSecretChatsForInvite - secretChat.secretChat.id,
                  privateGroups = state.privateGroups + (chatInfo._1.id -> (chatInfo._1 -> chatInfo._2)),
                  pipelineSecretChats = state.pipelineSecretChats +
                    (chatId -> newPipeline)
                )
              }
            } yield ()
          else if (secretChat.secretChat.state.isInstanceOf[TdApi.SecretChatStatePending] && !secretChat.secretChat.isOutbound) {
            for {
              _ <- client.send(new TdApi.OpenChat(secretChat.secretChat.id), SecretChatCreationHandler[F](userStateRef))
              state <- userStateRef.get
              _ <- userStateRef.update(
                _.copy(
                  secretChats = state.secretChats + (secretChat.secretChat.id -> secretChat.secretChat)
                )
              )
            } yield ()
          }
          else Sync[F].delay()
        } yield ()
      case TdApi.UpdateNewMessage.CONSTRUCTOR =>
        val msg = obj.asInstanceOf[TdApi.UpdateNewMessage]
        for {
          state <- userStateRef.get
          _ <- msg.message.content match {
            case a: MessageText =>
              Base64.decode(a.text.text) match {
                case Success(value) =>
                  StepMsgSerializer.parseBytes(value) match {
                    case Right(stepMsg) if !msg.message.isOutgoing =>
                      state.pipelineSecretChats.get(msg.message.chatId) match {
                        case Some(pipeline) => for {
                          newPipeLine <- pipeline.processInput(value)
                          _ <- userStateRef.update(_.copy(
                            pipelineSecretChats = state.pipelineSecretChats + (msg.message.chatId -> newPipeLine)))
                        } yield ()
                        case None => Pipelines.findStart(
                          userStateRef,
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
  }

  def setChatOrder(chat: TdApi.Chat, newOrder: Long): F[Unit] = {
    for {
      _ <- userStateRef.update(prevState =>
        if (prevState.chatList.length < 20) prevState.copy(
          chatList = prevState.mainChatList.takeRight(20).values.toList
        ) else prevState
      )
      _ <- Sync[F].delay(chat.order = newOrder)
      _ <- Logger[F].info(s"Got chat with id: ${chat.id}")
      _ <- userStateRef.update(prevState =>
        if (newOrder != 0) {
          prevState.javaState.get().setChatList(
            (chat :: prevState.chatList.filterNot(_.id == chat.id)).sortBy(_.order).takeRight(20).reverse.asJava
          )
          prevState.copy(
            chatList = (chat :: prevState.chatList.filterNot(_.id == chat.id)).sortBy(_.order).takeRight(20),
            mainChatList = (prevState.mainChatList.filterNot(_._2.id == chat.id) + (newOrder -> chat))
          )
        } else prevState
      )
    } yield ()
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
          code <- UserStateUtils.getVC(userStateRef)
          _ <- client.send(new TdApi.CheckAuthenticationCode(code), AuthRequestHandler())
        } yield ()
      case a: TdApi.AuthorizationStateWaitPassword =>
        for {
          pass <- UserStateUtils.getPass(userStateRef)
          _ <- client.send(new TdApi.CheckAuthenticationPassword(pass), AuthRequestHandler())
        } yield ()
      case a: TdApi.AuthorizationStateReady =>
        userStateRef.update{ prevState =>
          prevState.javaState.get().setAuth(true)
          prevState.copy(isAuth = true)
        }.map(_ => ()) >>
          client.send(
            new TdApi.GetChats(new TdApi.ChatListMain(), Long.MaxValue, 0, 20),
            EmptyHandler[F]()
          )
      case _ =>
        println(s"Got unknown event in auth. ${authEvent}").pure[F]
    }
  }

  def processLastMessage(msg: TdApi.Message): F[Unit] = {


    def msg2Str(msg: TdApi.Message, state: UserState[F]): String =
      msg.content match {
        case text: MessageText if state.privateGroups.contains(msg.chatId) =>
          val aes = AESEncryption(state.privateGroups(msg.chatId)._2.getBytes())
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
                                                   client: Client[F]): F[Handler[F]] = {
    val handler = new Handler(stateRef, privateConferenceService, client)
    for {
      list <- queueRef.get
      _ <- Sync[F].delay(list)
      _ <- list.foreach(elem => Sync[F].delay(println(s"Send: ${elem.getConstructor}. ${list}")) >> handler.onResult(elem)).pure[F]
      handlerExp <- handler.pure[F]
    } yield handlerExp
  }
}
