package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.{MVar, Ref}
import org.drinkless.tdlib.{Client, DummyHandler, TdApi}
import org.encryfoundation.tg.RunApp.getChats
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.drinkless.tdlib.TdApi.{ChatTypeSecret, MessagePhoto, MessageText, MessageVideo}
import org.encryfoundation.tg.ResultWithValueHandler
import org.encryfoundation.tg.leveldb.Database

case class PrintChats[F[_]: Concurrent: Timer](client: Client[F],
                                               userStateRef: Ref[F, UserState[F]],
                                               db: Database[F]) extends Command[F] {

  override val name: String = "showChats"

  override def run(args: List[String]): F[Unit] = for {
    state <- userStateRef.get
    dbChats <- db.get(Database.privateGroupChatsKey)
    _ <- Sync[F].delay(println(s"Chats: ${
      state.chatList.take(20).reverse.map { case (chat) =>
        if (state.privateGroups.contains(chat.id) ||
          dbChats.exists(_.map(_.toChar).mkString == chat.title)) chat.title ++ s". ChatId: ${chat.id} [Private group]."
        else if (chat.`type`.isInstanceOf[ChatTypeSecret])
          chat.title ++ s". ChatId: ${chat.id}. Secret chat id: ${chat.`type`.asInstanceOf[ChatTypeSecret].secretChatId} [Secret chat]"
        else chat.title ++ s". ChatId: ${chat.id}"
      }.mkString("\n ")}."))
  } yield ()

  def processLastMessage(msg: TdApi.Message): String =
    msg.content match {
      case text: MessageText => text.text.text
      case _: MessagePhoto => "photo"
      case _: MessageVideo => "video"
      case _ => "Unknown msg type"
    }
}
