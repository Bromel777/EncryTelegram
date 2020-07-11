package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{Client, DummyHandler, TdApi}
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.encryfoundation.tg.handlers.{CloseChatHandler, EmptyHandler}
import cats.implicits._
import org.encryfoundation.tg.services.ClientService

case class CloseChat[F[_]: Concurrent: Timer: Logger](userStateRef: Ref[F, UserState[F]],
                                                      db: Database[F])
                                                     (clientService: ClientService[F]) extends Command[F] {
  override val name: String = "closeChat"

  override def run(args: List[String]): F[Unit] = for {
    _ <- Sync[F].delay(println(s"Close chat. ${args.head.toLong}"))
    _ <- clientService.sendRequest(
      new TdApi.CloseChat(args.head.toLong),
      CloseChatHandler[F](userStateRef, args.head.toLong)
    )
  } yield ()
}
