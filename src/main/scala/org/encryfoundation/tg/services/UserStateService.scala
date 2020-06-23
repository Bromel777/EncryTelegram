package org.encryfoundation.tg.services

import cats.effect.Sync
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.TdApi
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.userState.{PrivateGroupChat, UserState}
import cats.implicits._

trait UserStateService[F[_]] {
  def persistChat(chat: TdApi.Chat): F[Unit]
  def persistSecretChat(chat: TdApi.SecretChat): F[Unit]
  def addPipelineChat(chat: TdApi.Chat, pipeline: Pipeline[F]): F[Unit]
  def updatePipelineChat(chat: TdApi.Chat, newPipeline: Pipeline[F]): F[Unit]
  def getPipeline(secretChatId: Long): F[Option[Pipeline[F]]]
  def upgradePendingSecretChat2Pipeline(chat: TdApi.Chat): F[Unit]
  def persistPrivateGroupChat(chat: TdApi.Chat, communityName: String, groupName: String, password: String): F[Unit]
  def recoverOrPersistChat(chat: TdApi.Chat): F[Unit]
}

object UserStateService {

  private final class Live[F[_]: Sync](userState: Ref[F, UserState[F]],
                                       db: Database[F]) extends UserStateService[F] {

    override def persistChat(chat: TdApi.Chat): F[Unit] = ???

    override def persistPrivateGroupChat(chat: TdApi.Chat,
                                         communityName: String,
                                         groupName: String,
                                         password: String): F[Unit] =
      userState.update( prevState =>
        prevState.copy(
          chatIds = prevState.chatIds + (chat.id -> chat),
          privateGroups = prevState.privateGroups + PrivateGroupChat(chat.id, communityName, groupName, password)
        )
      )

    override def recoverOrPersistChat(chat: TdApi.Chat): F[Unit] = ???

    override def upgradePendingSecretChat2Pipeline(chat: TdApi.Chat): F[Unit] = ???

    override def addPipelineChat(chat: TdApi.Chat, pipeline: Pipeline[F]): F[Unit] =
      userState.update( prevState =>
        prevState.copy(
          pipelineSecretChats = prevState.pipelineSecretChats +
            (chat.`type`.asInstanceOf[TdApi.ChatTypeSecret].secretChatId.toLong -> pipeline)
        )
      )

    override def updatePipelineChat(chat: TdApi.Chat, newPipeline: Pipeline[F]): F[Unit] = ???

    override def getPipeline(secretChatId: Long): F[Option[Pipeline[F]]] =
      userState.get.map(_.pipelineSecretChats.get(secretChatId))

    override def persistSecretChat(chat: TdApi.SecretChat): F[Unit] =
      userState.update(prevState =>
        prevState.copy(secretChats = prevState.secretChats + (chat.id -> chat))
      )
  }
}
