package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.encryfoundation.tg.EmptyHandler

case class Logout[F[_]: Concurrent: Timer](client: Client[F],
                                      userStateRef: Ref[F, UserState[F]],
                                      db: Database[F]) extends Command[F] {

  override val name: String = "logout"

  override def run(args: List[String]): F[Unit] = for {
    _ <- client.send(new TdApi.LogOut, EmptyHandler[F]())
  } yield ()
}
