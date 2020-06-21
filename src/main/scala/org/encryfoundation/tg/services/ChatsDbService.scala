package org.encryfoundation.tg.services

import cats.effect.concurrent.Ref
import org.drinkless.tdlib.TdApi
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState

trait ChatsDbService[F[_]] {
  def persistChat(chat: TdApi.Chat): F[Unit]
  def persistPrivateGroupChat(chat: TdApi.Chat, communityName: String, password: String): F[Unit]
  def recoverOrPersistChat(chat: TdApi.Chat): F[Unit]
}

object ChatsDbService {

  private final class Live[F[_]](userState: Ref[F, UserState[F]],
                                 db: Database[F]) extends ChatsDbService[F] {

    override def persistChat(chat: TdApi.Chat): F[Unit] = ???

    override def persistPrivateGroupChat(chat: TdApi.Chat,
                                         communityName: String,
                                         password: String): F[Unit] = ???

    override def recoverOrPersistChat(chat: TdApi.Chat): F[Unit] = ???
  }
}
