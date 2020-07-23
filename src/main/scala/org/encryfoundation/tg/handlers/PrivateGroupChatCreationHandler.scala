package org.encryfoundation.tg.handlers

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{Client, ResultHandler, TdApi}
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.services.{ClientService, PrivateConferenceService, UserStateService}
import org.encryfoundation.tg.userState.{PrivateGroupChat, UserState}
import cats.implicits._

case class PrivateGroupChatCreationHandler[F[_]: Concurrent: Timer: Logger](stateRef: Ref[F, UserState[F]],
                                                                            confInfo: PrivateCommunity,
                                                                            groupName: String,
                                                                            users: List[TdApi.User],
                                                                            myLogin: String,
                                                                            password: String)
                                                                           (privConfServ: PrivateConferenceService[F],
                                                                            userStateService: UserStateService[F],
                                                                            clientService: ClientService[F]) extends ResultHandler[F] {

  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Chat.CONSTRUCTOR =>
      val newChat = obj.asInstanceOf[TdApi.Chat]
      for {
        _ <- userStateService.persistPrivateGroupChat(
          PrivateGroupChat(newChat.id, confInfo.name, groupName, password)
        )
        _ <- users.traverse { user =>
          clientService.sendRequest(
            new TdApi.CreateNewSecretChat(user.id),
            SecretGroupPrivateChatCreationHandler[F](
              stateRef,
              PrivateGroupChat(newChat.id, confInfo.name, groupName, password),
              confInfo.name,
              password,
              user,
              newChat.id)(privConfServ, userStateService, clientService)
          )
        }
      } yield ()
    case err => Logger[F].info(s"Err during chat creation: ${obj}")
  }
}
