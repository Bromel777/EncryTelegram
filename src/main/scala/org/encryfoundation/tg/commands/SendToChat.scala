package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.RunApp.sendMessage
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import io.chrisdavenport.log4cats.Logger

case class SendToChat[F[_]: Concurrent: Timer: Logger](client: Client[F],
                                                       userStateRef: Ref[F, UserState[F]]) extends Command[F]{

  override val name: String = "sendToChat"

  override def run(args: List[String]): F[Unit] = for {
    state <- userStateRef.get
    recepient <- Sync[F].delay(args.dropRight(1).mkString(" "))
    _ <- Sync[F].delay(println(s"Recepient: ${recepient}. Users: ${state.mainChatList}"))
    _ <- Sync[F].delay(println(s"Recepient exists: " +
      s"${state.mainChatList.exists(_._2.title == recepient)}")
    )
    _ <- sendMessage(
      state.mainChatList.find(_._2.title == recepient).get._2.id,
      args.last,
      client
    )
  } yield ()
}
