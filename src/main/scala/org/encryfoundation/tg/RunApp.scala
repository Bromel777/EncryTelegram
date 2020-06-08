package org.encryfoundation.tg

import java.io.File
import java.security.Security

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ExitCode, IO, IOApp, Resource, Sync, Timer}
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.drinkless.tdlib.{Client, DummyHandler, TdApi}
import org.encryfoundation.tg.commands.Command
import org.encryfoundation.tg.errors.TdError
import org.encryfoundation.tg.handlers.{EmptyHandler, EmptyHandlerWithQueue, MessagesHandler}
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.programs.ConsoleProgram
import org.encryfoundation.tg.services.PrivateConferenceService
import org.encryfoundation.tg.userState.UserState
import tofu.Raise
import tofu._
import tofu.syntax.monadic._
import tofu.syntax.raise._
import scala.io.StdIn

object RunApp extends IOApp {

  System.loadLibrary("tdjni")

  def program(db: Database[IO]) = for {
    queueRef <- Ref.of[IO, List[TdApi.Object]](List.empty)
    implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]
    client <- Client[IO](EmptyHandlerWithQueue(queueRef))
    _ <- client.execute(new TdApi.SetLogVerbosityLevel(0))
    ref <- Ref.of[IO, UserState[IO]](UserState[IO](client = client))
    confService <- PrivateConferenceService[IO](db)
    handler <- Handler[IO](ref, queueRef, confService, client)
    _ <- client.setUpdatesHandler(handler)
  } yield (queueRef, client, ref, logger, confService)

  val database = for {
    db <- Database[IO](new File("db"))
  } yield db

  val anotherProg = Stream.resource(database).flatMap { db =>
    Stream.eval(program(db)).flatMap { case (queue, client, ref, logger, confService) =>
      implicit val loggerForIo = logger
      Stream.eval(ConsoleProgram[IO](client, ref, confService, db)).flatMap { consoleProgram =>
        client.run() concurrently consoleProgram.run()
      }
    }
  }

  def sendMessage[F[_]: Concurrent: Logger](chatId: Long, msg: String, client: Client[F]): F[Unit] = {
    val row: Array[TdApi.InlineKeyboardButton] = Array(
      new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()),
      new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()),
      new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())
    )
    val replyMarkup: TdApi.ReplyMarkup = new TdApi.ReplyMarkupInlineKeyboard(Array(row, row, row))
    val content: TdApi.InputMessageContent = new TdApi.InputMessageText(new TdApi.FormattedText(msg, null), false, true)
    client.send(new TdApi.SendMessage(chatId, 0, null, replyMarkup, content), EmptyHandler[F]())
  }

  override def run(args: List[String]): IO[ExitCode] =
    anotherProg.compile.drain.as(ExitCode.Success)
}
