package org.encryfoundation.tg.commands

import cats.data.OptionT
import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client, ClientUtils, TdApi}
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.services.ClientService
import org.encryfoundation.tg.utils.UserStateUtils

case class SendToChat[F[_]: Concurrent: Timer: Logger](userStateRef: Ref[F, UserState[F]],
                                                       clientService: ClientService[F]) extends Command[F]{

  override val name: String = "sendToChat"

  override def run(args: List[String]): F[Unit] =
    UserStateUtils.findChatByIdentifier(args.head, userStateRef).value.flatMap {
      case Some(chat) => ClientUtils.sendMsg(
        chat,
        args.last,
        userStateRef,
        clientService
      )
      case None => Logger[F].warn(s"Chat with identifier: ${args.head} doesn't exist")
    }
}
