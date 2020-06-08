package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.encryfoundation.tg.handlers.EmptyHandler

case class CloseSecretChat[F[_]: Concurrent: Timer: Logger](client: Client[F],
                                                            userStateRef: Ref[F, UserState[F]],
                                                            db: Database[F]) extends Command[F] {
  override val name: String = "closeSecretChat"

  override def run(args: List[String]): F[Unit] = for {
    _ <- client.send(
      new TdApi.CloseSecretChat(args.head.toInt),
      EmptyHandler[F]()
    )
  } yield ()
}
