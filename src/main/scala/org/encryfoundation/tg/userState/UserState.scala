package org.encryfoundation.tg.userState

import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.pipelines.Pipeline

import scala.collection.SortedMap
import java.util.concurrent.atomic.AtomicReference

import cats.{Applicative, FlatMap}
import cats.effect.Sync
import cats.effect.concurrent.MVar
import org.drinkless.tdlib.{Client, Client123, TdApi}
import org.encryfoundation.tg.leveldb.Database
import org.javaFX.model.JUserState
import scorex.crypto.hash.Blake2b256
import cats.implicits._

case class UserState[F[_]: Sync](chatList: List[TdApi.Chat] = List.empty,
                                 mainChatList: SortedMap[Long, TdApi.Chat] = SortedMap.empty[Long, TdApi.Chat],
                                 chatIds: Map[Long, TdApi.Chat] = Map.empty,
                                 privateGroups: Map[Long, (TdApi.Chat, String)] = Map.empty,
                                 pipelineSecretChats: Map[Long, Pipeline[F]] = Map.empty[Long, Pipeline[F]],
                                 pendingSecretChatsForInvite: Map[Long, (TdApi.Chat, String, TdApi.User)] = Map.empty,
                                 users: Map[Int, TdApi.User] = Map.empty,
                                 basicGroups: Map[Int, TdApi.BasicGroup] = Map.empty,
                                 superGroups: Map[Int, TdApi.Supergroup] = Map.empty,
                                 secretChats: Map[Int, TdApi.SecretChat] = Map.empty,
                                 isAuth: Boolean = false,
                                 client: Client[F],
                                 activeChat: Long = 0,
                                 javaState: AtomicReference[JUserState],
                                 db: Database[F]) {

  def updatePrivateGroups(newGroupChat: TdApi.Chat, pass: String): F[UserState[F]] =
    for {
      _ <- db.put(UserState.privateGroupChatKey(newGroupChat.id), pass.getBytes)
    } yield this.copy(
      privateGroups = privateGroups + (newGroupChat.id -> (newGroupChat, pass))
    )

  def checkChat(chat: TdApi.Chat): F[UserState[F]] =
    db.get(UserState.privateGroupChatKey(chat.id)).flatMap {
      case Some(bytes) =>
        Applicative[F].pure(this.copy(privateGroups = privateGroups + (chat.id -> (chat, bytes.map(_.toChar).mkString))))
      case None => Applicative[F].pure(this)
    }
}

object UserState {
  def privateGroupChatKey(chatId: Long): Array[Byte] = Blake2b256.hash(chatId + "privateChat")
}