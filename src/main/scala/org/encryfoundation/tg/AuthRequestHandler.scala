package org.encryfoundation.tg

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{Client123, ResultHandler, TdApi}
import org.encryfoundation.tg.javaIntegration.AuthMsg
import org.encryfoundation.tg.services.UserStateService
import org.encryfoundation.tg.steps.Step
import org.encryfoundation.tg.userState.UserState

case class AuthRequestHandler[F[_]: Sync: Logger](userState: Ref[F, UserState[F]]) extends ResultHandler[F] {
  /**
   * Callback called on result of query to TDLib or incoming update from TDLib.
   *
   * @param obj Result of query or update of type TdApi.Update about new events.
   */
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Error.CONSTRUCTOR =>
      userState.get.flatMap { state =>
        state.currentStep match {
          case Step.AuthStep =>
            Sync[F].delay(state.javaState.get().authQueue.put(AuthMsg.Error)) >> Logger[F].info(s"Error occured at step: ${state.currentStep}. Error: ${obj}")
          case _ => Logger[F].info(s"Error occured at step: ${state.currentStep}. Error: ${obj}")
        }
      }
    case TdApi.Ok.CONSTRUCTOR =>
      ().pure[F]
    case _ =>
      Sync[F].delay(println("Any"))
  }
}
