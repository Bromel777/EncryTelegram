package org.encryfoundation.tg.handlers

import cats.Applicative
import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.{MVar, Ref}
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{ResultHandler, TdApi}
import org.encryfoundation.tg.userState.UserState
import cats.implicits._

case class ValueHandler[F[_]: Concurrent: Timer: Logger, RT,
                        M <: TdApi.Object](stateRef: Ref[F, UserState[F]],
                                           returnMVar: MVar[F, RT],
                                           valueParser: M => F[RT]) extends ResultHandler[F] {

  override def onResult(obj: TdApi.Object): F[Unit] = obj match {
    case m: M => valueParser(m).flatMap(value => returnMVar.put(value))
    case _ => Applicative[F].pure(())
  }
}
