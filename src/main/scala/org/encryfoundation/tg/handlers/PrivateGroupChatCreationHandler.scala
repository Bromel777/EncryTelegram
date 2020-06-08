package org.encryfoundation.tg.handlers

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{Client, ResultHandler, TdApi}
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.services.PrivateConferenceService
import org.encryfoundation.tg.userState.UserState
import cats.implicits._

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
            SecretGroupPrivateChatCreationHandler[F](
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
