package org.encryfoundation.tg

import java.io.File
import java.util.concurrent.atomic.AtomicReference

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ContextShift, IO, Timer}
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.handlers.{EmptyHandler, EmptyHandlerWithQueue}
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.programs.{ConsoleProgram, UIProgram}
import org.encryfoundation.tg.services.PrivateConferenceService
import org.encryfoundation.tg.userState.UserState
import org.javaFX.EncryWindow
import org.javaFX.model.JUserState
import scorex.crypto.encode.Base64
import tofu.syntax.monadic._

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
    ref <- Ref.of[IO, UserState[IO]](UserState[IO](client = client, javaState = state, db = db))
    confService <- PrivateConferenceService[IO](db, ref)
    handler <- Handler[IO](ref, queueRef, confService, client)
    _ <- client.setUpdatesHandler(handler)
  } yield (queueRef, client, ref, logger, confService)

  val database = for {
    db <- Database[IO](new File("db"))
  } yield db

  def anotherProg(state: AtomicReference[JUserState]) = Stream.resource(database).flatMap { db =>
    Stream.eval(program(db, state)).flatMap { case (queue, client, ref, logger, confService) =>
      implicit val loggerForIo = logger
      Stream.eval(ConsoleProgram[IO](client, ref, confService, db)).flatMap { consoleProgram =>
        Stream.eval(UIProgram(ref, confService)).flatMap { uiProg =>
          client.run() concurrently consoleProgram.run() concurrently uiProg.run()
        }
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

  def sendMsg[F[_]: Concurrent: Logger](chat: TdApi.Chat, msg: String, stateRef: Ref[F, UserState[F]]): F[Unit] = {

    stateRef.get.flatMap(state =>
      state.privateGroups.find(_._2._1.id == chat.id).map { case (_, (_, pass)) =>
        val aes = AESEncryption(pass.getBytes())
        sendMessage(chat.id, Base64.encode(aes.encrypt(msg.getBytes)), state.client)
      }.getOrElse(sendMessage(chat.id, msg, state.client))
    )
  }

  anotherProg(EncryWindow.state).compile.drain.unsafeRunAsyncAndForget()
  EncryWindow.main(Array.empty)

}
