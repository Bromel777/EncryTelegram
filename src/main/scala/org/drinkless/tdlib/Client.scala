package org.drinkless.tdlib

import cats.effect.concurrent.{Ref, Semaphore}
import cats.effect.{Concurrent, Fiber, Sync, Timer}
import cats.implicits._
import fs2.Stream

import scala.concurrent.duration._

case class Client[F[_]: Concurrent: Timer] private(readLock: Semaphore[F],
                                                   writeLock: Semaphore[F],
                                                   isClientDestroyed: Ref[F, Boolean],
                                                   currentQueryId: Ref[F, Long],
                                                   handlersRef: Ref[F, Map[Long, Handler[F]]]) {

  private val MAX_EVENTS = 1000
  private var eventIds = new Array[Long](MAX_EVENTS)
  private var events = new Array[TdApi.Object](MAX_EVENTS)
  private var stopFlag = false

  var nativeClientId = createNativeClient()

  def execute(query: TdApi.Function): F[TdApi.Object] = nativeClientExecute(query).pure[F]

  //todo: add isClientDestroyed check
  def send(query: TdApi.Function,
           resultHandler: ResultHandler[F],
           exceptionHandler: Option[ExceptionHandler[F]] = None): F[Unit] = for {
    _ <- readLock.acquire
    queryId <- currentQueryId.get
    _ <- currentQueryId.update(_ => queryId + 1)
    _ <- handlersRef.update(prevMap => prevMap + (queryId + 1 -> Handler(resultHandler, exceptionHandler)))
    _ <- Sync[F].delay(nativeClientSend(nativeClientId, queryId + 1, query))
    _ <- readLock.release
  } yield ()

  def setUpdatesHandler(updatesHandler: ResultHandler[F],
                        exceptionHandler: Option[ExceptionHandler[F]] = None): F[Unit] = for {
    _ <- handlersRef.tryUpdate(prev => prev.updated(0L, Handler[F](updatesHandler, exceptionHandler)))
  } yield ()

  def receiveQueries(timeout: Double): Stream[F, Unit] = {
    Stream(())
      .covary[F]
      .repeat
      .evalMap { _ =>
        (0 until nativeClientReceive(nativeClientId, eventIds, events, timeout)).toList.traverse(i =>
          if (!stopFlag) {
            processResult(eventIds(i), events(i)).flatMap(_ => Sync[F].delay({
              events(i) = null
            }))
          } else ().pure[F]) >> ().pure[F]
      }
  }

  private def processResult(id: Long, obj: TdApi.Object): F[Unit] = for {
    handlers <- handlersRef.get
    _ <- if (id == 0) {
      val newHandler = handlers.get(id)
      handlersRef
        .update(_ => handlers)
        .flatMap(_ => newHandler.map(handler => handleResult(obj, handler)).getOrElse(().pure[F]))
    } else {
      val newHandler = handlers.get(id)
      handlersRef
        .update(_ => handlers - id)
        .flatMap(_ => newHandler.map(handler => handleResult(obj, handler)).getOrElse(().pure[F]))
    }
  } yield ()

  //todo: Add exception handling
  private def handleResult(obj: TdApi.Object,
                           handler: Handler[F]): F[Unit] = handler.resultHandler.onResult(obj)

  @native
  private def createNativeClient(): Long

  @native
  private def nativeClientSend(nativeClientId: Long, eventId: Long, function: TdApi.Function): Unit

  @native
  private def nativeClientReceive(nativeClientId: Long,
                                  eventIds: Array[Long],
                                  events: Array[TdApi.Object],
                                  timeout: Double): Int

  @native
  private def nativeClientExecute(function: TdApi.Function): TdApi.Object

  @native
  private def destroyNativeClient(nativeClientId: Long): Unit

  def run(): Stream[F, Unit] = receiveQueries(300)
}

object Client {

  def apply[F[_]: Concurrent: Timer](requestHandler: ResultHandler[F]): F[Client[F]] = for {
    readLockSem <- Semaphore[F](1)
    writeLockSem <- Semaphore[F](1)
    isClientDestroyed <- Ref.of[F, Boolean](false)
    currentQueryId <- Ref.of[F, Long](0: Long)
    handlers <- Ref.of[F, Map[Long, Handler[F]]](Map((0: Long) -> Handler(requestHandler)))
  } yield Client[F](readLockSem, writeLockSem, isClientDestroyed, currentQueryId, handlers)
}
