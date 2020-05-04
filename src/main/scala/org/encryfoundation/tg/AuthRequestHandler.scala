package org.encryfoundation.tg

import cats.effect.Sync
import cats.implicits._
import org.drinkless.tdlib.{Client123, ResultHandler, TdApi}

case class AuthRequestHandler[F[_]: Sync]() extends ResultHandler[F] {
  /**
   * Callback called on result of query to TDLib or incoming update from TDLib.
   *
   * @param obj Result of query or update of type TdApi.Update about new events.
   */
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Error.CONSTRUCTOR =>
      Sync[F].delay(println(s"Err occured. $obj"))
    case TdApi.Ok.CONSTRUCTOR =>
      ().pure[F]
    case _ =>
      Sync[F].delay(println("Any"))
  }
}
