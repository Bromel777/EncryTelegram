package org.encryfoundation.tg.handlers

import cats.effect.Concurrent
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{ResultHandler, TdApi}
import cats.implicits._

case class EmptyHandler[F[_]: Concurrent: Logger]() extends ResultHandler[F] {
  /**
   * Callback called on result of query to TDLib or incoming update from TDLib.
   *
   * @param obj Result of query or update of type TdApi.Update about new events.
   */
  override def onResult(obj: TdApi.Object): F[Unit] = {
    //().pure[F]
    for {
      _ <- Logger[F].info(s"Receive $obj on empty handler")
    } yield ()
  }
}
