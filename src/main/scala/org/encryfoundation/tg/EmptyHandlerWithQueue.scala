package org.encryfoundation.tg

import cats.effect.{Concurrent, Sync, Timer}
import cats.implicits._
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.TdApi.{MessageText, Messages}
import org.drinkless.tdlib.{Client, Client123, ResultHandler, TdApi}
import org.encryfoundation.tg.community.{PrivateCommunity, PrivateCommunityStatus}
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.pipelines.groupVerification.ProverFirstStep
import org.encryfoundation.tg.services.PrivateConferenceService
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
      _ <- Concurrent[F].delay(println(s"Receive $obj on empty handler"))
    } yield ()
  }
}

case class SecretChatHandler[F[_]: Concurrent: Logger](stateRef: Ref[F, UserState[F]]) extends ResultHandler[F]{
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Chat.CONSTRUCTOR =>
      for {
        state <- stateRef.get
        _ <- Logger[F].info(s"Receive secret: ${obj}")
        _ <- stateRef.update(
          _.copy(
            mainChatList = state.mainChatList + (obj.asInstanceOf[TdApi.Chat].id -> obj.asInstanceOf[TdApi.Chat]),
          )
        )
      } yield ()
    case _ => ().pure[F]
  }
}

case class SecretGroupPrivateChatHandler[F[_]: Concurrent: Timer: Logger](stateRef: Ref[F, UserState[F]],
                                                                  confname: String,
                                                                  pass: String,
                                                                  recipient: TdApi.User,
                                                                  confChatId: Long,
                                                                  client: Client[F])(privConfServ: PrivateConferenceService[F]) extends ResultHandler[F]{
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Chat.CONSTRUCTOR =>
      for {
        state <- stateRef.get
        pipeLineStep <- ProverFirstStep(
          client,
          stateRef,
          confname,
          recipient.phoneNumber,
          pass,
          obj.asInstanceOf[TdApi.Chat],
          obj.asInstanceOf[TdApi.Chat].id
        )(privConfServ)
        _ <- stateRef.update(
          _.copy(
            mainChatList = state.mainChatList + (obj.asInstanceOf[TdApi.Chat].id -> obj.asInstanceOf[TdApi.Chat]),
            pendingSecretChatsForInvite = state.pendingSecretChatsForInvite + (
              obj.asInstanceOf[TdApi.Chat].`type`.asInstanceOf[TdApi.ChatTypeSecret].secretChatId.toLong -> (
                obj.asInstanceOf[TdApi.Chat],
                confname,
                recipient
              )),
            pipelineSecretChats = state.pipelineSecretChats + (obj.asInstanceOf[TdApi.Chat].id -> pipeLineStep)
          )
        )
      } yield ()
    case _ => ().pure[F]
  }
}

case class PrivateGroupChatCreationHandler[F[_]: Concurrent: Timer: Logger](stateRef: Ref[F, UserState[F]],
                                                                            client: Client[F],
                                                                            confInfo: PrivateCommunity,
                                                                            users: List[TdApi.User],
                                                                            myLogin: String,
                                                                            password: String)(privConfServ: PrivateConferenceService[F]) extends ResultHandler[F] {
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Chat.CONSTRUCTOR =>
      val newChat = obj.asInstanceOf[TdApi.Chat]
      for {
        state <- stateRef.get
        _ <- Sync[F].delay(println("New chat created!"))
        _ <- stateRef.update(
          _.copy(
            chatIds = state.chatIds + (newChat.id -> newChat),
            privateGroups = state.privateGroups + (newChat.id -> (newChat, password))
          )
        )
        _ <- users.traverse { user =>
          client.send(
            new TdApi.CreateNewSecretChat(user.id),
            SecretGroupPrivateChatHandler[F](
              stateRef,
              confInfo.name,
              password,
              user,
              newChat.id,
              client)(privConfServ)
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