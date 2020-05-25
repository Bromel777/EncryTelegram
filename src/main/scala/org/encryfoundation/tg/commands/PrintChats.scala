package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client, DummyHandler, TdApi}
import org.encryfoundation.tg.RunApp.getChats
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.drinkless.tdlib.TdApi.ChatTypeSecret
import org.encryfoundation.tg.leveldb.Database

case class PrintChats[F[_]: Concurrent: Timer](client: Client[F],
                                               userStateRef: Ref[F, UserState[F]],
                                               db: Database[F]) extends Command[F] {

  override val name: String = "showChats"

  override def run(args: List[String]): F[Unit] = for {
    state <- userStateRef.get
    _ <- if (state.isAuth) client.send(
      new TdApi.GetChats(new TdApi.ChatListMain(), Long.MaxValue, 0, 20),
      DummyHandler[F](client, userStateRef, (cl, ref) => getChats(cl, ref))
    ) else Sync[F].delay(println("State auth - false"))
    _ <- if (state.mainChatList.length < 20) run(args) else for {
      dbChats <- db.get(Database.privateGroupChatsKey)
      _ <- Sync[F].delay(println(s"Chats: ${
        state.mainChatList.take(20).map(chat =>
          if (state.privateGroups.contains(chat.id) ||
            dbChats.exists(_.map(_.toChar).mkString == chat.title)) chat.title ++ s" [Private group]"
          else if(chat.`type`.isInstanceOf[ChatTypeSecret]) chat.title ++ s" [Secret chat]"
          else chat.title
        ).mkString("\n ")}."))
    } yield ()
  } yield ()
}
