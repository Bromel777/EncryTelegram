package org.encryfoundation.tg.commands

import cats.effect.Sync
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.Client
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import scorex.crypto.encode.Base16

case class ShowPrivateConferences[F[_]: Sync](client: Client[F],
                                              userStateRef: Ref[F, UserState[F]],
                                              db: Database[F]) extends Command[F] {

  override val name: String = "showConferences"

  override def run(args: List[String]): F[Unit] = for {
    confPosName <- db.get(s"conf".getBytes())
    _ <- if (confPosName.isEmpty) Sync[F].delay(println("There is no private confs"))
         else for {
            confName <- Sync[F].delay(confPosName.get.map(_.toChar).mkString)
            userKsi <- db.get(s"conf${confName}MySecreteKsi".getBytes()).map(result => Base16.encode(result.get))
            userT <- db.get(s"conf${confName}MySecreteT".getBytes()).map(result => Base16.encode(result.get))
            _ <- Sync[F].delay(println(s"ConfName: ${confName}. My Ksi: ${userKsi}. My T: ${userT}"))
         }  yield ()
  } yield ()
}
