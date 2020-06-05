package org.drinkless.tdlib

import cats.Applicative
import cats.effect.Sync

trait ResultHandler[F[_]] {

  def onResult(obj: TdApi.Object): F[Unit]
}