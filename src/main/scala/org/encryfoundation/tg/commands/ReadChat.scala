package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.MessagesHandler
import org.encryfoundation.tg.userState.UserState
import cats.implicits._

case class ReadChat[F[_]: Concurrent: Timer](client: Client[F],
                                             userStateRef: Ref[F, UserState[F]]) extends Command[F] {

  override val name: String = "read"

  override def run(args: List[String]): F[Unit] = for {
    state <- userStateRef.get
    _ <- client.send(
      new TdApi.GetChatHistory(args.last.toLong, 0, 0, 20, false),
      MessagesHandler[F](state.privateGroups.find(_._1 == args.last.toLong).map(_._2._2))
    )
  } yield ()
}
