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
  def addPrivateGroupChat(newChat: PrivateGroupChat): F[Unit]
  def getPrivateGroupChat(chatId: Long): F[Option[PrivateGroupChat]]
  def addPipelineChat(chat: TdApi.Chat, newPipeline: Pipeline[F]): F[Unit]
  def updatePipelineChat(secretChatId: Long, newPipeline: Pipeline[F]): F[Unit]
  def getPipeline(secretChatId: Long): F[Option[Pipeline[F]]]
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

    override def addPipelineChat(chat: TdApi.Chat, pipeline: Pipeline[F]): F[Unit] =
      userState.update(prevState =>
        prevState.copy(
          pipelineSecretChats = prevState.pipelineSecretChats +
            (chat.`type`.asInstanceOf[TdApi.ChatTypeSecret].secretChatId.toLong -> pipeline)
        )
      )

    override def updatePipelineChat(secretChatId: Long, newPipeline: Pipeline[F]): F[Unit] = for {
      state <- userState.get
      isPending <- state.pendingSecretChatsForInvite.contains(secretChatId).pure[F]
      _ <- if (isPending) userState.update { prevState =>
        prevState.copy(
          pendingSecretChatsForInvite = prevState.pendingSecretChatsForInvite - secretChatId,
          pipelineSecretChats = state.pipelineSecretChats + (secretChatId -> newPipeline)
        )
      } else userState.update { prevState =>
        prevState.copy(
          pipelineSecretChats = state.pipelineSecretChats + (secretChatId -> newPipeline)
        )
      }
    } yield ()

    override def getPipeline(secretChatId: Long): F[Option[Pipeline[F]]] =
      userState.get.map(_.pipelineSecretChats.get(secretChatId))

    override def persistSecretChat(chat: TdApi.SecretChat): F[Unit] =
      userState.update(prevState =>
        prevState.copy(secretChats = prevState.secretChats + (chat.id -> chat))
      )

    override def addPrivateGroupChat(newChat: PrivateGroupChat): F[Unit] =
      userState.update(prevState =>
        prevState.copy(privateGroups = prevState.privateGroups + newChat)
      )

    override def getPrivateGroupChat(chatId: Long): F[Option[PrivateGroupChat]] =
      userState.get.map(_.privateGroups.find(_.chatId == chatId))
  }

  def apply[F[_]: Sync](userState: Ref[F, UserState[F]],
                        db: Database[F]): F[UserStateService[F]] = Sync[F].delay(new Live[F](userState, db))
}
