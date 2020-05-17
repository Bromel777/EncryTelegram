package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.Client
import org.encryfoundation.tg.RunApp.sendMessage
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.encryfoundation.tg.leveldb.Database
import scorex.crypto.encode.Base16

case class WriteSecure[F[_]: Concurrent: Timer](client: Client[F],
                                                userStateRef: Ref[F, UserState[F]],
                                                db: Database[F]) extends Command[F] {

  override val name: String = "writeSecure"

  override def run(args: List[String]): F[Unit] = for {
    state <- userStateRef.get
    passDb <- db.get(args.head.getBytes())
    secureMsg <- Sync[F].delay{
      val pass =
        state.privateGroups
          .find(_._2._1.title == args.head)
          .map(_._2._2)
          .orElse(passDb.map(_.map(_.toChar).mkString))
          .get
      val aes = AESEncryption(pass.getBytes())
      Base16.encode(aes.encrypt(args.last.getBytes()))
    }
    _ <- sendMessage(state.chatIds.find(_._2.title == args.head).get._1, secureMsg, client)
  } yield ()
}
