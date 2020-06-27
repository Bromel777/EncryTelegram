package org.encryfoundation.tg.services

import cats.effect.Sync
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.TdApi
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.userState.{PrivateGroupChat, PrivateGroupChats, UserState}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger

trait UserStateService[F[_]] {
  def persistChat(chat: TdApi.Chat): F[Unit]
  def persistSecretChat(chat: TdApi.SecretChat): F[Unit]
  def getPrivateGroupChat(chatId: Long): F[Option[PrivateGroupChat]]
  def addPipelineChat(chat: TdApi.Chat, newPipeline: Pipeline[F]): F[Unit]
  def updatePipelineChat(secretChatId: Long, newPipeline: Pipeline[F]): F[Unit]
  def addPendingPipelineChat(chat: TdApi.Chat,
                             newPipeline: Pipeline[F]): F[Unit]
  def getPipeline(chatId: Long): F[Option[Pipeline[F]]]
  def persistPrivateGroupChat(privateGroupChat: PrivateGroupChat): F[Unit]
  def recoverOrPersistChat(chat: TdApi.Chat): F[Unit]
}

object UserStateService {

  private final class Live[F[_]: Sync: Logger](userState: Ref[F, UserState[F]],
                                               db: Database[F]) extends UserStateService[F] {

    override def persistChat(chat: TdApi.Chat): F[Unit] = ???

    override def persistPrivateGroupChat(privateGroupChat: PrivateGroupChat): F[Unit] =
      userState.updateAndGet(prevState =>
        prevState.copy(
          privateGroups = prevState.privateGroups + privateGroupChat
        )
      ).flatMap(newState =>
        db.put(UserState.privateChatsKey, PrivateGroupChats.toBytes(newState.privateGroups.toList))
      ) >> Logger[F].info(s"Persist private group chat: ${privateGroupChat}")

    override def recoverOrPersistChat(chat: TdApi.Chat): F[Unit] = ???

    override def addPipelineChat(chat: TdApi.Chat, pipeline: Pipeline[F]): F[Unit] =
      userState.update(prevState =>
        prevState.copy(
          pipelineSecretChats = prevState.pipelineSecretChats + (chat.id -> pipeline)
        )
      )

    override def updatePipelineChat(chatId: Long, newPipeline: Pipeline[F]): F[Unit] = for {
      state <- userState.get
      pending <- state.pendingSecretChatsForInvite.get(chatId.toInt).pure[F]
      _ <- if (pending.nonEmpty) userState.update { prevState =>
        prevState.copy(
          pendingSecretChatsForInvite = prevState.pendingSecretChatsForInvite - chatId.toInt,
          pipelineSecretChats = state.pipelineSecretChats + (pending.get._1.id -> newPipeline)
        )
      } else userState.update { prevState =>
        prevState.copy(
          pipelineSecretChats = state.pipelineSecretChats + (chatId -> newPipeline)
        )
      }
    } yield ()

    override def getPipeline(secretChatId: Long): F[Option[Pipeline[F]]] = for {
      state <- userState.get
      pendingPipeLine <- state.pendingSecretChatsForInvite.find(_._1 == secretChatId.toInt).map(_._2._2).pure[F]
      privatePipeline <- userState.get.map(_.pipelineSecretChats.get(secretChatId))
    } yield pendingPipeLine.orElse(privatePipeline)

    override def persistSecretChat(chat: TdApi.SecretChat): F[Unit] =
      userState.update(prevState =>
        prevState.copy(secretChats = prevState.secretChats + (chat.id -> chat))
      )

    override def getPrivateGroupChat(chatId: Long): F[Option[PrivateGroupChat]] =
      userState.get.map(_.privateGroups.find(_.chatId == chatId))

    override def addPendingPipelineChat(chat: TdApi.Chat,
                                        newPipeline: Pipeline[F]): F[Unit] = chat.`type` match {
      case secret: TdApi.ChatTypeSecret =>
        Logger[F].info(s"Add chat with id: ${chat.id} and secret chat id: ${secret.secretChatId}") >>
        userState.update { prevState =>
          prevState.copy(
            pendingSecretChatsForInvite = prevState.pendingSecretChatsForInvite +
              (secret.secretChatId -> (chat, newPipeline))
          )
        }
      case _ =>  ().pure[F]
    }
  }

  def apply[F[_]: Sync: Logger](userState: Ref[F, UserState[F]],
                               db: Database[F]): F[UserStateService[F]] = Sync[F].delay(new Live[F](userState, db))
}
