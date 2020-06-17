package org.encryfoundation.tg.handlers

import cats.Applicative
import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.{MVar, Ref}
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{ResultHandler, TdApi}
import org.encryfoundation.tg.userState.UserState
import cats.implicits._

case class AccumulatorHandler[F[_]: Concurrent: Timer: Logger,
                              RT,
                              M <: TdApi.Object](stateRef: Ref[F, UserState[F]],
                                                 returnMVar: MVar[F, List[RT]],
                                                 limit: Int,
                                                 accumulator: Ref[F, List[RT]],
                                                 valueParser: M => F[RT]) extends ResultHandler[F] {

  override def onResult(obj: TdApi.Object): F[Unit] = obj match {
    case m: M => for {
      currentAccumulator <- accumulator.get
      _ <- Sync[F].delay(println(s"Receive: $m"))
      processedMsg <- valueParser(m)
      _ <- if (currentAccumulator.length == limit) returnMVar.put(currentAccumulator)
           else accumulator.set(currentAccumulator :+ processedMsg)
    } yield ()
    case _ => Applicative[F].pure(())
  }
}

object AccumulatorHandler {
  def apply[F[_]: Concurrent: Timer: Logger,
            RT,
            M <: TdApi.Object](stateRef: Ref[F, UserState[F]],
                               returnMVar: MVar[F, List[RT]],
                               limit: Int,
                               valueParser: M => F[RT]): F[AccumulatorHandler[F, RT, M]] = for {
    accumulator <- Ref.of[F, List[RT]](List.empty[RT])
  } yield AccumulatorHandler(
    stateRef,
    returnMVar,
    limit,
    accumulator,
    valueParser
  )
}
