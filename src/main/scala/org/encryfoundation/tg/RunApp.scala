package org.encryfoundation.tg

import java.io.File
import java.security.Security

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ExitCode, IO, IOApp, Resource, Sync, Timer}
import cats.implicits._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.drinkless.tdlib.{Client, DummyHandler, TdApi}
import org.encryfoundation.tg.commands.Command
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.services.PrivateConferenceService
import org.encryfoundation.tg.userState.UserState

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
      client.run() concurrently Stream.eval(regComm(client, ref, confService, db))
    }
  }

  def getChats[F[_]: Concurrent: Timer](client: Client[F], userStateRef: Ref[F, UserState[F]]): F[Unit] = for {
    state <- userStateRef.get
    _ <- if (state.isAuth) client.send(
      new TdApi.GetChats(new TdApi.ChatListMain(), Long.MaxValue, 0, 20),
      DummyHandler[F](client, userStateRef, (cl, ref) => getChats(cl, ref))
    ) else Sync[F].delay(println("State auth - false"))
    _ <- Sync[F].delay(println(s"Chats: ${
      state.mainChatList.take(20).map(chat =>
        if (state.privateGroups.contains(chat.id)) chat.title ++ s" [Private group chat]"
        else chat.title
      ).mkString("\n ")}."))
  } yield ()

  def regComm[F[_]: Concurrent: Timer: Logger](client: Client[F],
                                               userStateRef: Ref[F, UserState[F]],
                                               confService: PrivateConferenceService[F],
                                               db: Database[F]): F[Unit] = for {
    state <- userStateRef.get
    _ <- if (state.isAuth) onlyForReg(client, userStateRef, confService, db)
         else regComm(client, userStateRef, confService, db)
  } yield ()

  def onlyForReg[F[_]: Concurrent: Timer: Logger](client: Client[F],
                                                  userStateRef: Ref[F, UserState[F]],
                                                  confService: PrivateConferenceService[F],
                                                  db: Database[F]): F[Unit] =
    for {
      command <- Sync[F].delay {
        println("Write command. ('list')")
        val command = StdIn.readLine()
        println(s"Your command: ${command}.")
        command
      }
      _ <- Command.getCommands(client, userStateRef, db)(confService).find(_.name == command.split(" ").head)
        .traverse(_.run(command.split(" ").tail.toList))
      _ <- onlyForReg(client, userStateRef, confService, db)
    } yield ()

  def sendMessage[F[_]: Concurrent](chatId: Long, msg: String, client: Client[F]): F[Unit] = {
    val row: Array[TdApi.InlineKeyboardButton] = Array(
      new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()),
      new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()),
      new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())
    )
    val replyMarkup: TdApi.ReplyMarkup = new TdApi.ReplyMarkupInlineKeyboard(Array(row, row, row))
    val content: TdApi.InputMessageContent = new TdApi.InputMessageText(new TdApi.FormattedText(msg, null), false, true)
    client.send(new TdApi.SendMessage(chatId, 0, null, replyMarkup, content), EmptyHandler[F]())
  }

  def getChatMessages[F[_]: Concurrent](chatId: Long, client: Client[F], stateRef: Ref[F, UserState[F]]): F[Unit] = {
    for {
      state <- stateRef.get
      _ <- client.send(
        new TdApi.GetChatHistory(chatId, 0, 0, 20, false),
        MessagesHandler[F](state.privateGroups.find(_._1 == chatId).map(_._2._2))
      )
    } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] =
    anotherProg.compile.drain.as(ExitCode.Success)
}
