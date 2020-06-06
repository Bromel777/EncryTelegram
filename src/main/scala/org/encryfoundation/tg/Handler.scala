package org.encryfoundation.tg

import java.math.BigInteger

import cats.Applicative
import cats.effect.concurrent.{MVar, Ref}
import cats.effect.{ConcurrentEffect, Sync, Timer}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.drinkless.tdlib.TdApi.MessageText
import org.drinkless.tdlib.{Client, ResultHandler, TdApi}
import org.encryfoundation.mitmImun.{Prover, Verifier}
import org.encryfoundation.tg.RunApp.sendMessage
import org.encryfoundation.tg.community.PrivateCommunityStatus.UserCommunityStatus.AwaitingSecondPhaseFromUser
import org.encryfoundation.tg.pipelines.Pipelines
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.GroupVerificationStepMsg.ProverFirstStepMsg
import org.encryfoundation.tg.pipelines.groupVerification.{ProverFirstStep, VerifierSecondStep}
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.StartPipeline
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializer
import org.encryfoundation.tg.services.PrivateConferenceService
import org.encryfoundation.tg.userState.UserState
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
          _ <- setChatOrder(chat, newOrder)
        } yield ()
      case TdApi.UpdateUser.CONSTRUCTOR =>
        val updateUser = obj.asInstanceOf[TdApi.UpdateUser]
        for {
          state <- userStateRef.get
          _ <- userStateRef.update(_.copy(users = state.users + (updateUser.user.id -> updateUser.user)))
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
              _ <- userStateRef.update(
                _.copy(
                  pendingSecretChatsForInvite = state.pendingSecretChatsForInvite - secretChat.secretChat.id,
                  privateGroups = state.privateGroups + (chatInfo._1.id -> (chatInfo._1 -> chatInfo._2)),
                  pipelineSecretChats = state.pipelineSecretChats +
                    (chatId -> newPipeline)
                )
              )
            } yield ()
          else if (secretChat.secretChat.state.isInstanceOf[TdApi.SecretChatStatePending] && !secretChat.secretChat.isOutbound) {
            for {
              _ <- client.send(new TdApi.OpenChat(secretChat.secretChat.id), SecretChatHandler[F](userStateRef))
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
                    case Right(_) => (()).pure[F]
                    case Left(_) => Logger[F].info(s"Receive msg: ${msg}")
                  }
                case Failure(err) => Logger[F].info(s"Receive unkown elem1: ${obj}. Err: ${err.getMessage}")
              }
            case _ => Logger[F].info(s"Receive unkown elem2: ${obj}")
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
      _ <- userStateRef.update(prevState =>
        if (newOrder != 0) prevState.copy(
          chatList = (chat :: prevState.chatList.filterNot(_.id == chat.id)).sortBy(_.order).takeRight(20),
          mainChatList = (prevState.mainChatList + (newOrder -> chat))
        ) else prevState
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
        println("Enter phone number:")
        val phoneNumber = StdIn.readLine()
        client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), AuthRequestHandler())
      case a: TdApi.AuthorizationStateWaitCode =>
        println("Enter code number:")
        val code = StdIn.readLine()
        client.send(new TdApi.CheckAuthenticationCode(code), AuthRequestHandler())
      case a: TdApi.AuthorizationStateWaitPassword =>
        println("Enter password")
        val pass = StdIn.readLine()
        client.send(new TdApi.CheckAuthenticationPassword(pass), AuthRequestHandler())
      case a: TdApi.AuthorizationStateReady =>
        userStateRef.update(_.copy(isAuth = true)).map(_ => ())
      case _ =>
        println(s"Got unknown event in auth. ${authEvent}").pure[F]
    }
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
