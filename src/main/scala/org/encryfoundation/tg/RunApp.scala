package org.encryfoundation.tg

import java.io.File
import java.util.concurrent.atomic.AtomicReference

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ContextShift, IO, Resource, Sync, Timer}
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.handlers.EmptyHandlerWithQueue
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.programs.{ConsoleProgram, UIProgram}
import org.encryfoundation.tg.services.{ClientService, PrivateConferenceService, UserStateService}
import org.encryfoundation.tg.userState.UserState
import org.javaFX.EncryWindow
import org.javaFX.model.JUserState
import cats.implicits._

import scala.concurrent.ExecutionContext

object RunApp extends App {

  implicit val shift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  val a = IO.delay("test")

  System.loadLibrary("tdjni")

  def program(db: Database[IO],
              state: AtomicReference[JUserState]) = for {
    queueRef <- Ref.of[IO, List[TdApi.Object]](List.empty)
    implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]
    mainState <- UserState.recoverOrCreate(state, db)
    ref <- Ref.of[IO, UserState[IO]](mainState)
    services <- produceServices(db, ref)
  } yield (queueRef, ref, logger, services)

  val database = Database[IO](new File("db"))

  def produceServices[F[_]: ConcurrentEffect: Timer: Logger](db: Database[F],
                                                             stateRef: Ref[F, UserState[F]])
  : F[(PrivateConferenceService[F], UserStateService[F], ClientService[F])] =
    for {
      confService <- PrivateConferenceService[F](db, stateRef)
      userStateService <- UserStateService[F](db, stateRef)
      clientService <- ClientService[F](confService, userStateService, stateRef)
    } yield (confService, userStateService, clientService)

  def anotherProg(state: AtomicReference[JUserState]) = Stream.resource(database).flatMap { db =>
    Stream.eval(program(db, state)).flatMap { case (queue, ref, logger, (conf, user, client)) =>
      implicit val loggerForIo = logger
      Stream.eval(UIProgram(ref, conf, user, client)).flatMap { uiProg =>
        client.runClient() concurrently uiProg.run()
      }
    }
  }

  anotherProg(EncryWindow.state).compile.drain.unsafeRunAsyncAndForget()
  EncryWindow.main(Array.empty)

}
