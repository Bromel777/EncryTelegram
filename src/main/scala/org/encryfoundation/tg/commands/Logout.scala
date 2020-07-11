package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.handlers.EmptyHandler
import org.encryfoundation.tg.services.ClientService

case class Logout[F[_]: Concurrent: Timer: Logger](clientService: ClientService[F],
                                                   userStateRef: Ref[F, UserState[F]],
                                                   db: Database[F]) extends Command[F] {

  override val name: String = "logout"

  override def run(args: List[String]): F[Unit] = for {
    _ <- clientService.sendRequest(new TdApi.LogOut, EmptyHandler[F]())
  } yield ()
}
