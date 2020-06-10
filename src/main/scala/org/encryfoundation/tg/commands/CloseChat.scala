package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{Client, DummyHandler, TdApi}
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.encryfoundation.tg.handlers.{CloseChatHandler, EmptyHandler}

case class CloseChat[F[_]: Concurrent: Timer: Logger](client: Client[F],
                                                      userStateRef: Ref[F, UserState[F]],
                                                      db: Database[F]) extends Command[F] {
  override val name: String = "closeChat"

  override def run(args: List[String]): F[Unit] = for {
    _ <- client.send(
      new TdApi.CloseChat(args.head.toLong),
      CloseChatHandler[F](userStateRef, client, args.head.toLong)
    )
  } yield ()
}
