package org.encryfoundation.tg

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.TdApi.MessageText
import org.drinkless.tdlib.{Client, ResultHandler, TdApi}
import org.encryfoundation.tg.userState.UserState

import scala.io.StdIn

case class Handler[F[_]: ConcurrentEffect: Logger](userStateRef: Ref[F, UserState[F]]) extends ResultHandler[F] {

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
        val order = chat.order
        chat.order = 0
        for {
          state <- userStateRef.get
          _ <- userStateRef.update(_.copy(chatIds = state.chatIds + (chat.id -> chat)))
          _ <- setChatOrder(chat, order)
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
        } yield ()
      case any => Logger[F].info(s"Receive unkown elem: ${obj}")
    }
  }

  def setChatOrder(chat: TdApi.Chat, order: Long): F[Unit] = {
    for {
      state <- userStateRef.get
      //_ <- Sync[F].delay(println(s"Get chat order: ${order}"))
      _ <- if (chat.order != 0)
            userStateRef.update(_.copy(mainChatList = state.mainChatList.filter(_.order == chat.order)))
          else (()).pure[F]
      _ <- Sync[F].delay(chat.order = order)
      _ <- if (order != 0)
            userStateRef.update(
              _.copy(
                mainChatList = (chat :: state.mainChatList).sortBy(_.order).takeRight(20).reverse
              )
            )
          else (()).pure[F]
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
  def apply[F[_]: ConcurrentEffect: Logger](stateRef: Ref[F, UserState[F]],
                                            queueRef: Ref[F, List[TdApi.Object]]): F[Handler[F]] = {
    val handler = new Handler(stateRef)
    for {
      list <- queueRef.get
      _ <- Sync[F].delay(list)
      _ <- list.foreach(elem => Sync[F].delay(println(s"Send: ${elem.getConstructor}. ${list}")) >> handler.onResult(elem)).pure[F]
      handlerExp <- handler.pure[F]
    } yield handlerExp
  }
}
