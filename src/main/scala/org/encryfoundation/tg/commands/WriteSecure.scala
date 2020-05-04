package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.Client
import org.encryfoundation.common.utils.Algos
import org.encryfoundation.tg.RunApp.sendMessage
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.userState.UserState
import cats.implicits._

case class WriteSecure[F[_]: Concurrent: Timer](client: Client[F],
                                                userStateRef: Ref[F, UserState[F]]) extends Command[F] {

  override val name: String = "writeSecure"

  override def run(args: List[String]): F[Unit] = for {
    state <- userStateRef.get
    secureMsg <- Sync[F].delay{
      val pass = state.privateGroups.find(_._1 == args.tail.head.toLong).get._2._2
      val aes = AESEncryption(pass.getBytes())
      Algos.encode(aes.encrypt(args.last.getBytes()))
    }
    _ <- sendMessage(args.drop(1).head.toLong, secureMsg, client)
  } yield ()
}
