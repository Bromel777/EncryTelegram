package org.encryfoundation.tg.handlers

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{Client, ResultHandler, TdApi}
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.services.{PrivateConferenceService, UserStateService}
import org.encryfoundation.tg.userState.{PrivateGroupChat, UserState}
import cats.implicits._

case class PrivateGroupChatCreationHandler[F[_]: Concurrent: Timer: Logger](stateRef: Ref[F, UserState[F]],
                                                                            client: Client[F],
                                                                            confInfo: PrivateCommunity,
                                                                            groupName: String,
                                                                            users: List[TdApi.User],
                                                                            myLogin: String,
                                                                            password: String)
                                                                           (privConfServ: PrivateConferenceService[F],
                                                                            userStateService: UserStateService[F]) extends ResultHandler[F] {

  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Chat.CONSTRUCTOR =>
      val newChat = obj.asInstanceOf[TdApi.Chat]
      for {
        _ <- Sync[F].delay(println("New chat created!"))
        _ <- userStateService.persistPrivateGroupChat(
          newChat,
          confInfo.name,
          groupName,
          password
        )
        _ <- users.traverse { user =>
          client.send(
            new TdApi.CreateNewSecretChat(user.id),
            SecretGroupPrivateChatCreationHandler[F](
              stateRef,
              PrivateGroupChat(newChat.id, confInfo.name, groupName, password),
              confInfo.name,
              password,
              user,
              newChat.id,
              client)(privConfServ, userStateService)
          )
        }
      } yield ()
    case err => Logger[F].info(s"Err during chat creation: ${obj}")
  }
}
