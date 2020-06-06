package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.{MVar, Ref}
import org.drinkless.tdlib.{Client, DummyHandler, TdApi}
import org.encryfoundation.tg.RunApp.getChats
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.drinkless.tdlib.TdApi.ChatTypeSecret
import org.encryfoundation.tg.ResultWithValueHandler
import org.encryfoundation.tg.leveldb.Database

case class PrintChats[F[_]: Concurrent: Timer](client: Client[F],
                                               userStateRef: Ref[F, UserState[F]],
                                               db: Database[F]) extends Command[F] {

  override val name: String = "showChats"

  override def run(args: List[String]): F[Unit] = for {
    _ <- Sync[F].delay(println("init showChats"))
    state <- userStateRef.get
    _ <- if (state.isAuth && state.chatList.length < 20) client.send(
      new TdApi.GetChats(new TdApi.ChatListMain(), Long.MaxValue, 0, 20),
      DummyHandler[F](client, userStateRef, (cl, ref) => getChats(cl, ref))
    ) else Sync[F].delay(println("State auth - false"))
    _ <- Sync[F].delay(println(s"trying to print chats. state.chatList.length: ${state.chatList.length}"))
    _ <- if (state.chatList.length < 20) run(args) else for {
      dbChats <- db.get(Database.privateGroupChatsKey)
      _ <- Sync[F].delay(println(s"Chats: ${
        state.chatList.take(20).reverse.map { case (chat) =>
          if (state.privateGroups.contains(chat.id) ||
            dbChats.exists(_.map(_.toChar).mkString == chat.title)) chat.title ++ s" [Private group]."
          else if (chat.`type`.isInstanceOf[ChatTypeSecret]) chat.title ++ s" [Secret chat]"
          else chat.title ++ s". Order: ${chat.order}"
        }.mkString("\n ")}."))
    } yield ()
  } yield ()
}
