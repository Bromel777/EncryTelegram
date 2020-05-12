package org.encryfoundation.tg

import java.io.File

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ContextShift, ExitCode, IO, Resource, Sync, Timer}
import cats.implicits._
import fs2.Stream
import org.drinkless.tdlib.{Client, DummyHandler, TdApi}
import org.encryfoundation.tg.commands.{CreatePrivateGroup, PrintChats, ReadChat, WriteSecure}
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState
import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control.{ButtonType, Dialog, _}
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.{GridPane, HBox}
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{LinearGradient, Stops}
import scalafx.scene.text.Text

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object RunApp extends JFXApp {

  System.loadLibrary("tdjni")

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  val program = for {
    queueRef <- Ref.of[IO, List[TdApi.Object]](List.empty)
    client <- Client[IO](EmptyHandlerWithQueue(queueRef))
    _ <- client.execute(new TdApi.SetLogVerbosityLevel(0))
    ref <- Ref.of[IO, UserState[IO]](UserState[IO](client = client))
    handler <- Handler(ref, queueRef)
    _ <- client.setUpdatesHandler(handler)
  } yield (queueRef, client, ref)

  val database = for {
    file <- Resource.make[IO, File](IO.delay(new File("db")))(_ => IO.delay(println("File closed!")))
    _ <- Resource.pure[IO, Boolean](file.mkdir())
    db <- Database[IO](file)
  } yield db

  val anotherProg = Stream.eval(program).flatMap { case (queue, client, ref) =>
    Stream.resource(database).flatMap { db =>
      client.run() concurrently Stream.eval(regComm(client, ref, db))
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
        if (state.privateGroups.contains(chat.id)) chat.title ++ s" [Private group] (${chat.id})"
        else chat.title ++ s" (${chat.id})"
      ).mkString("\n ")}."))
  } yield ()

  def regComm[F[_]: Concurrent: Timer](client: Client[F],
                                       userStateRef: Ref[F, UserState[F]],
                                       db: Database[F]): F[Unit] = for {
    state <- userStateRef.get
    _ <- if (state.isAuth) onlyForReg(client, userStateRef, db) else regComm(client, userStateRef, db)
  } yield ()

  def onlyForReg[F[_]: Concurrent: Timer](client: Client[F],
                                          userStateRef: Ref[F, UserState[F]],
                                          db: Database[F]): F[Unit] =
    for {
      command <- Sync[F].delay {
        println("Write command. ('list')")
        val command = StdIn.readLine()
        println(s"Your command: ${command}.")
        command
      }
      _ <- if (command == "list")
              PrintChats[F](client, userStateRef, db).run(command.split(" ").tail.toList)
           else if (command.split(" ").head == "write")
              WriteSecure[F](client, userStateRef, db).run(command.split(" ").tail.toList)
           else if (command.split(" ").head == "createPrivateGroup") {
            Sync[F].delay(CreatePrivateGroup[F](client, userStateRef, db)).flatMap{ handler =>
              handler.run(command.split(" ").tail.toList)
            }
          } else if (command.split(" ").head == "read") {
              ReadChat[F](client, userStateRef, db).run(command.split(" ").tail.toList)
          } else if (command.split(" ").head == "writeSecure") {
              WriteSecure[F](client, userStateRef, db).run(command.split(" ").tail.toList)
          } else (Sync[F].delay(println(s"Receive unkown command: ${command}")))
        _ <- onlyForReg(client, userStateRef, db)
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

  anotherProg.compile.drain.as(ExitCode.Success).unsafeRunAsyncAndForget()
}
