package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Sync}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{Client, DummyHandler, TdApi}
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.drinkless.tdlib.TdApi.ChatTypeSecret
import org.encryfoundation.tg.EmptyHandler
import org.encryfoundation.tg.RunApp.sendMessage

case class SendToSecretChat[F[_]: Concurrent: Logger](client: Client[F],
                                                      userStateRef: Ref[F, UserState[F]],
                                                      db: Database[F]) extends Command[F] {

  override val name: String = "sendToSecretChat"

  override def run(args: List[String]): F[Unit] = for {
    state <- userStateRef.get
    userLogin <- args.dropRight(1).pure[F]
    _ <- Sync[F].delay(println(s"Chat login: ${userLogin.mkString(" ")}"))
    chatId <- state.mainChatList.find {
      chat => chat.`type`.isInstanceOf[ChatTypeSecret] && chat.title == userLogin.mkString(" ")
    }.get.id.pure[F]
    _ <- Sync[F].delay(println(s"Find secret chat id: ${chatId}"))
    _ <- sendMessage(
      chatId,
      args.last,
      client
    )
  } yield ()
}
