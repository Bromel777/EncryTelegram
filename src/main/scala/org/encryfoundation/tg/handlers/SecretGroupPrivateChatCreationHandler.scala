package org.encryfoundation.tg.handlers

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{Client, ResultHandler, TdApi}
import org.encryfoundation.tg.pipelines.groupVerification.ProverFirstStep
import org.encryfoundation.tg.services.PrivateConferenceService
import org.encryfoundation.tg.userState.UserState
import cats.implicits._

case class SecretGroupPrivateChatCreationHandler[F[_]: Concurrent: Timer: Logger](stateRef: Ref[F, UserState[F]],
                                                                                  confname: String,
                                                                                  pass: String,
                                                                                  recipient: TdApi.User,
                                                                                  confChatId: Long,
                                                                                  client: Client[F])(privConfServ: PrivateConferenceService[F]) extends ResultHandler[F]{
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Chat.CONSTRUCTOR =>
      for {
        state <- stateRef.get
        pipeLineStep <- ProverFirstStep(
          client,
          stateRef,
          confname,
          recipient.phoneNumber,
          pass,
          obj.asInstanceOf[TdApi.Chat],
          obj.asInstanceOf[TdApi.Chat].id
        )(privConfServ)
        _ <- stateRef.update(
          _.copy(
            mainChatList = state.mainChatList + (obj.asInstanceOf[TdApi.Chat].id -> obj.asInstanceOf[TdApi.Chat]),
            pendingSecretChatsForInvite = state.pendingSecretChatsForInvite + (
              obj.asInstanceOf[TdApi.Chat].`type`.asInstanceOf[TdApi.ChatTypeSecret].secretChatId.toLong -> (
                obj.asInstanceOf[TdApi.Chat],
                confname,
                recipient
              )),
            pipelineSecretChats = state.pipelineSecretChats + (obj.asInstanceOf[TdApi.Chat].id -> pipeLineStep)
          )
        )
      } yield ()
    case _ => ().pure[F]
  }
}
