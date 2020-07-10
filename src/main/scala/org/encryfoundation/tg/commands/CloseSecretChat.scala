package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.encryfoundation.tg.handlers.{CloseChatHandler, EmptyHandler}
import org.encryfoundation.tg.services.ClientService

case class CloseSecretChat[F[_]: Concurrent: Timer: Logger](clientService: ClientService[F],
                                                            userStateRef: Ref[F, UserState[F]],
                                                            db: Database[F]) extends Command[F] {
  override val name: String = "closeSecretChat"

  override def run(args: List[String]): F[Unit] = for {
    _ <- clientService.sendRequest(
      new TdApi.CloseSecretChat(args.head.toInt),
      CloseChatHandler[F](userStateRef, args.last.toLong)
    )
  } yield ()
}
