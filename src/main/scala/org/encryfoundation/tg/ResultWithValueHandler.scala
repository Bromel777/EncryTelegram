package org.encryfoundation.tg

import cats.Applicative
import cats.effect.Concurrent
import cats.effect.concurrent.MVar
import org.drinkless.tdlib.{ResultHandler, TdApi}
import cats.implicits._

case class ResultWithValueHandler[F[_]: Applicative, T <: TdApi.Object](result: MVar[F, T]) extends ResultHandler[F] {
  override def onResult(obj: TdApi.Object): F[Unit] = obj match {
    case res: T => result.put(res)
    case _ =>  Applicative[F].pure(())
  }
}
