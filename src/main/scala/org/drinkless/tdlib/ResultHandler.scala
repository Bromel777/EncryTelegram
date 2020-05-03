package org.drinkless.tdlib

trait ResultHandler[F[_]] {

  def onResult(obj: TdApi.Object): F[Unit]
}
