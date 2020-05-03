package org.drinkless.tdlib

trait ExceptionHandler[F[_]] {

  def onException(e: Throwable): F[Unit]
}
