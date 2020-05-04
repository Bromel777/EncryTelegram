package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.Client
import org.encryfoundation.tg.RunApp.sendMessage
import org.encryfoundation.tg.userState.UserState
import cats.implicits._

case class SendTo[F[_]: Concurrent: Timer](client: Client[F],
                                           userStateRef: Ref[F, UserState[F]]) extends Command[F]{

  override val name: String = "sendTo"

  override def run(args: List[String]): F[Unit] = for {
    state <- userStateRef.get
    recepient <- Sync[F].delay(args.tail.dropRight(1).mkString(" "))
    _ <- Sync[F].delay(println(s"Recepient: ${recepient}. Users: ${state.users}"))
    _ <- Sync[F].delay(println(s"Recepient exists: " +
      s"${state.chatIds.exists(_._2.title == recepient)}")
    )
    _ <- sendMessage(
      state.chatIds.find(_._2.title == recepient).get._2.id,
      args.last,
      client
    )
  } yield ()
}
