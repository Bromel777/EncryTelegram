package org.encryfoundation.tg

import java.io.File
import java.util.concurrent.atomic.AtomicReference

import cats.effect.concurrent.Ref
import cats.effect.{ContextShift, IO, Timer}
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.handlers.EmptyHandlerWithQueue
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.programs.{ConsoleProgram, UIProgram}
import org.encryfoundation.tg.services.{PrivateConferenceService, UserStateService}
import org.encryfoundation.tg.userState.UserState
import org.javaFX.EncryWindow
import org.javaFX.model.JUserState

import scala.concurrent.ExecutionContext

object RunApp extends App {

  implicit val shift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  System.loadLibrary("tdjni")

  def program(db: Database[IO],
              state: AtomicReference[JUserState]) = for {
    queueRef <- Ref.of[IO, List[TdApi.Object]](List.empty)
    implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]
    client <- Client[IO](EmptyHandlerWithQueue(queueRef))
    _ <- client.execute(new TdApi.SetLogVerbosityLevel(0))
    mainState <- UserState.recoverOrCreate(client, state, db)
    _ <- Logger[IO].info("State recovered successfully")
    ref <- Ref.of[IO, UserState[IO]](mainState)
    confService <- PrivateConferenceService[IO](db, ref)
    userStateService <- UserStateService[IO](ref, db)
    handler <- Handler[IO](ref, queueRef, confService, userStateService, client)
    _ <- client.setUpdatesHandler(handler)
  } yield (queueRef, client, ref, logger, confService, userStateService)

  val database = for {
    db <- Database[IO](new File("db"))
  } yield db

  def anotherProg(state: AtomicReference[JUserState]) = Stream.resource(database).flatMap { db =>
    Stream.eval(program(db, state)).flatMap { case (queue, client, ref, logger, confService, userStateService) =>
      implicit val loggerForIo = logger
      Stream.eval(ConsoleProgram[IO](client, ref, confService, userStateService, db)).flatMap { consoleProgram =>
        Stream.eval(UIProgram(ref, confService, userStateService, client)).flatMap { uiProg =>
          client.run() concurrently consoleProgram.run() concurrently uiProg.run()
        }
      }
    }
  }

  anotherProg(EncryWindow.state).compile.drain.unsafeRunAsyncAndForget()
  EncryWindow.main(Array.empty)

}
