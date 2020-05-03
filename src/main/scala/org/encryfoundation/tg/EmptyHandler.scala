package org.encryfoundation.tg

import cats.effect.{Concurrent, Sync}
import cats.implicits._
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client123, ResultHandler, TdApi}

case class EmptyHandler[F[_]: Concurrent](queue: Ref[F, List[TdApi.Object]]) extends ResultHandler[F] {
  /**
   * Callback called on result of query to TDLib or incoming update from TDLib.
   *
   * @param obj Result of query or update of type TdApi.Update about new events.
   */
  override def onResult(obj: TdApi.Object): F[Unit] = {
    for {
      _ <- Concurrent[F].delay(println(s"Get smth: ${obj}"))
      _ <- queue.update(list => list :+ obj)
    } yield ()
  }
}
