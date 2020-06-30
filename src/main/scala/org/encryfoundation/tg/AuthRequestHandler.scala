package org.encryfoundation.tg

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import org.drinkless.tdlib.{Client123, ResultHandler, TdApi}
import org.encryfoundation.tg.javaIntegration.AuthMsg
import org.encryfoundation.tg.services.UserStateService
import org.encryfoundation.tg.steps.Step
import org.encryfoundation.tg.userState.UserState

case class AuthRequestHandler[F[_]: Sync](userState: Ref[F, UserState[F]]) extends ResultHandler[F] {
  /**
   * Callback called on result of query to TDLib or incoming update from TDLib.
   *
   * @param obj Result of query or update of type TdApi.Update about new events.
   */
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Error.CONSTRUCTOR =>
      userState.get.map { state =>
        state.currentStep match {
          case Step.AuthStep =>
            state.javaState.get().authQueue.put(AuthMsg.Error)
          case _ => ()
        }
      }
      Sync[F].delay(println(s"Err occured. $obj"))
    case TdApi.Ok.CONSTRUCTOR =>
      ().pure[F]
    case _ =>
      Sync[F].delay(println("Any"))
  }
}
