package org.encryfoundation.tg

import cats.effect.{Concurrent, Sync}
import cats.implicits._
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.TdApi.{MessageText, Messages}
import org.drinkless.tdlib.{Client, Client123, ResultHandler, TdApi}
import org.encryfoundation.tg.community.{PrivateCommunity, PrivateCommunityStatus}
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base16

import scala.util.Try

case class EmptyHandlerWithQueue[F[_]: Concurrent](queue: Ref[F, List[TdApi.Object]]) extends ResultHandler[F] {
  /**
   * Callback called on result of query to TDLib or incoming update from TDLib.
   *
   * @param obj Result of query or update of type TdApi.Update about new events.
   */
  override def onResult(obj: TdApi.Object): F[Unit] = {
    for {
      //_ <- Concurrent[F].delay(println(s"Get smth: ${obj}"))
      _ <- queue.update(list => list :+ obj)
    } yield ()
  }
}

case class EmptyHandler[F[_]: Concurrent]() extends ResultHandler[F] {
  /**
   * Callback called on result of query to TDLib or incoming update from TDLib.
   *
   * @param obj Result of query or update of type TdApi.Update about new events.
   */
  override def onResult(obj: TdApi.Object): F[Unit] = {
    //().pure[F]
    for {
      _ <- Concurrent[F].delay(println(s"Get smth EMPTY HANDLER: ${obj}"))
    } yield ()
  }
}

case class SecretChatHandler[F[_]: Concurrent](stateRef: Ref[F, UserState[F]]) extends ResultHandler[F]{
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Chat.CONSTRUCTOR =>
      for {
        state <- stateRef.get
        _ <- Sync[F].delay(println(s"Receive: ${obj}"))
        _ <- stateRef.update(
          _.copy(
            mainChatList = obj.asInstanceOf[TdApi.Chat] +: state.mainChatList,
          )
        )
      } yield ()
    case _ => ().pure[F]
  }
}

case class SecretGroupPrivateChatHandler[F[_]: Concurrent](stateRef: Ref[F, UserState[F]],
                                                           confname: String) extends ResultHandler[F]{
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Chat.CONSTRUCTOR =>
      for {
        state <- stateRef.get
        _ <- Sync[F].delay(println(s"Receive private chat for pending: ${obj}"))
        _ <- stateRef.update(
          _.copy(
            mainChatList = obj.asInstanceOf[TdApi.Chat] +: state.mainChatList,
            pendingSecretChatsForInvite = state.pendingSecretChatsForInvite + (
              obj.asInstanceOf[TdApi.Chat].`type`.asInstanceOf[TdApi.ChatTypeSecret].secretChatId.toLong -> (obj.asInstanceOf[TdApi.Chat], confname)
            )
          )
        )
      } yield ()
    case _ => ().pure[F]
  }
}

case class PrivateGroupChatCreationHandler[F[_]: Concurrent: Logger](stateRef: Ref[F, UserState[F]],
                                                                     client: Client[F],
                                                                     confInfo: PrivateCommunity,
                                                                     userIds: List[Long],
                                                                     myLogin: String,
                                                                     password: String) extends ResultHandler[F] {
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Chat.CONSTRUCTOR =>
      val newChat = obj.asInstanceOf[TdApi.Chat]
      for {
        state <- stateRef.get
        _ <- Sync[F].delay(println("New chat created!"))
        _ <- stateRef.update(
          _.copy(
            chatIds = state.chatIds + (newChat.id -> newChat),
            privateGroups = state.privateGroups + (newChat.id ->
              (newChat, password, PrivateCommunityStatus.getNewInfoForChat(myLogin, password, confInfo)))
          )
        )
        _ <- userIds.traverse { userId =>
          client.send(
            new TdApi.CreateNewSecretChat(userId.toInt),
            SecretGroupPrivateChatHandler[F](stateRef, confInfo.name)
          )
        }
      } yield ()
    case err => Logger[F].info(s"Err during chat creation: ${obj}")
  }
}

case class MessagesHandler[F[_]: Concurrent](password: Option[String]) extends ResultHandler[F] {

  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Messages.CONSTRUCTOR =>
      val messages = obj.asInstanceOf[Messages]
      val msgs = password.map { pass =>
        val aes = AESEncryption(pass.getBytes())
        messages.messages.map {
          _.content match {
            case txtMsg: MessageText =>
              Try(aes.decrypt(Base16.decode(txtMsg.text.text).get).map(_.toChar).mkString)
                .getOrElse(txtMsg.text.text)
            case _ => "Unknown msg!"
          }
        }
      } getOrElse(Array.empty[String])
      Sync[F].delay(println("Messages:" + msgs.mkString("\n ")))
    case TdApi.Message.CONSTRUCTOR =>
      Sync[F].delay(println("Receive message"))
    case _ => ().pure[F]
  }
}