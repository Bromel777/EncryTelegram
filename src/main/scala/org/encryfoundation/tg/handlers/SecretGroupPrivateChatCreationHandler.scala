package org.encryfoundation.tg.handlers

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{Client, ResultHandler, TdApi}
import org.encryfoundation.tg.pipelines.groupVerification.ProverFirstStep
import org.encryfoundation.tg.services.{ClientService, PrivateConferenceService, UserStateService}
import org.encryfoundation.tg.userState.{PrivateGroupChat, UserState}
import cats.implicits._

case class SecretGroupPrivateChatCreationHandler[F[_]: Concurrent: Timer: Logger](stateRef: Ref[F, UserState[F]],
                                                                                  privateGroupChat: PrivateGroupChat,
                                                                                  confname: String,
                                                                                  pass: String,
                                                                                  recipient: TdApi.User,
                                                                                  confChatId: Long)
                                                                                 (privConfServ: PrivateConferenceService[F],
                                                                                  stateService: UserStateService[F],
                                                                                  clientService: ClientService[F]) extends ResultHandler[F]{
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Chat.CONSTRUCTOR =>
      for {
        state <- stateRef.get
        pipeLineStep <- ProverFirstStep(
          stateRef,
          privateGroupChat,
          confname,
          recipient.phoneNumber,
          pass,
          obj.asInstanceOf[TdApi.Chat],
          obj.asInstanceOf[TdApi.Chat].id
        )(privConfServ, stateService, clientService)
        _ <- Logger[F].info(s"Receive secret chat. Add chat to pending. Chat id: ${obj.asInstanceOf[TdApi.Chat].id}")
        _ <- stateService.addPendingPipelineChat(obj.asInstanceOf[TdApi.Chat], pipeLineStep)
      } yield ()
    case _ => ().pure[F]
  }
}
