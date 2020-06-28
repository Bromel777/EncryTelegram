package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.encryfoundation.tg.handlers.MessagesHandler
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.services.UserStateService

case class ReadChat[F[_]: Concurrent: Timer](client: Client[F],
                                             userStateRef: Ref[F, UserState[F]],
                                             db: Database[F])
                                            (userStateService: UserStateService[F]) extends Command[F] {

  override val name: String = "read"

  override def run(args: List[String]): F[Unit] = for {
    state <- userStateRef.get
    chat <- state.mainChatList.find(_._2.title == args.last).get._2.pure[F]
    dbPass <- db.get(args.last.getBytes())
    groupInfo <- userStateService.getPrivateGroupChat(chat.id)
    _ <- client.send(
      new TdApi.GetChatHistory(chat.id, 0, 0, 20, false),
      MessagesHandler[F](
        groupInfo.map(_.password).orElse(dbPass.map(_.map(_.toChar).mkString)))
    )
  } yield ()
}
