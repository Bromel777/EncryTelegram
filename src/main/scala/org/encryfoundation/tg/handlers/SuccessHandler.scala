package org.encryfoundation.tg.handlers

import cats.effect.Concurrent
import cats.effect.concurrent.MVar
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{ResultHandler, TdApi}

case class SuccessHandler[F[_]: Concurrent: Logger](container: MVar[F, Boolean]) extends ResultHandler[F] {
  override def onResult(obj: TdApi.Object): F[Unit] = obj match {
    case _: TdApi.Ok => container.put(true)
    case _ => container.put(false)
  }
}
