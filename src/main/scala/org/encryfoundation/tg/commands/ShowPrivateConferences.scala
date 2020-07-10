package org.encryfoundation.tg.commands

import cats.effect.Sync
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.Client
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.encryfoundation.tg.services.PrivateConferenceService
import scorex.crypto.encode.Base16

case class ShowPrivateConferences[F[_]: Sync](userStateRef: Ref[F, UserState[F]],
                                              db: Database[F]) (
                                              privateConfService: PrivateConferenceService[F]) extends Command[F]{

  override val name: String = "showConferences"

  override def run(args: List[String]): F[Unit] = for {
    confs <- privateConfService.getConfs
    _ <- Sync[F].delay(println(s"Active conferences: ${confs}"))
  } yield ()
}
