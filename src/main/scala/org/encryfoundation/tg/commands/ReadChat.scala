package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.MessagesHandler
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.encryfoundation.tg.leveldb.Database

case class ReadChat[F[_]: Concurrent: Timer](client: Client[F],
                                             userStateRef: Ref[F, UserState[F]],
                                             db: Database[F]) extends Command[F] {

  override val name: String = "read"

  override def run(args: List[String]): F[Unit] = for {
    state <- userStateRef.get
    chat <- state.mainChatList.find(_.title == args.last).get.pure[F]
    dbPass <- db.get(args.last.getBytes())
    _ <- client.send(
      new TdApi.GetChatHistory(chat.id, 0, 0, 20, false),
      MessagesHandler[F](
        (state.privateGroups.find(_._1 == chat.id).map(_._2._2))
          .orElse(dbPass.map(_.map(_.toChar).mkString)))
    )
  } yield ()
}
