package org.encryfoundation.tg.handlers

import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{ResultHandler, TdApi}
import cats.implicits._

case class EmptyHandlerWithQueue[F[_]: Concurrent](queue: Ref[F, List[TdApi.Object]]) extends ResultHandler[F] {
  /**
   * Callback called on result of query to TDLib or incoming update from TDLib.
   *
   * @param obj Result of query or update of type TdApi.Update about new events.
   */
  override def onResult(obj: TdApi.Object): F[Unit] = {
    for {
      //_ <- Concurrent[F].delay(println(s"Get smth: ${obj}"))
      _ <- queue.update(list => list :+ obj)
    } yield ()
  }
}
