package org.encryfoundation.tg

import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.{Concurrent, ContextShift, ExitCode, IO, IOApp, Sync, Timer}
import cats.syntax.applicative._
import cats.effect.concurrent.Ref
import cats.effect.internals.IOAppPlatform
import org.drinkless.tdlib.{Client, Client123, DummyHandler, TdApi}
import org.encryfoundation.tg.userState.UserState
import fs2.Stream
import cats.implicits._
import org.drinkless.tdlib.Client123.ResultHandler
import org.encryfoundation.common.utils.Algos
import org.encryfoundation.tg.RunApp.onlyForReg
import org.encryfoundation.tg.commands.{CreatePrivateGroup, PrintChats, ReadChat, WriteSecure}
import org.encryfoundation.tg.crypto.AESEncryption

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{LinearGradient, Stops}
import scalafx.scene.text.Text
import scalafx.stage.PopupWindow.AnchorLocation.WindowBottomRight

import scala.io.StdIn

object RunApp extends IOApp {

  System.loadLibrary("tdjni")

  val program = for {
    queueRef <- Ref.of[IO, List[TdApi.Object]](List.empty)
    client <- Client[IO](EmptyHandlerWithQueue(queueRef))
    _ <- client.execute(new TdApi.SetLogVerbosityLevel(0))
    ref <- Ref.of[IO, UserState[IO]](UserState[IO](client = client))
    handler <- Handler(ref, queueRef)
    _ <- client.setUpdatesHandler(handler)
  } yield (queueRef, client, ref)

  val anotherProg = Stream.eval(program).flatMap { case (queue, client, ref) =>
    client.run() concurrently Stream.eval(regComm(client, ref))
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

  def regComm[F[_]: Concurrent: Timer](client: Client[F], userStateRef: Ref[F, UserState[F]]): F[Unit] = for {
    state <- userStateRef.get
    _ <- if (state.isAuth) onlyForReg(client, userStateRef) else regComm(client, userStateRef)
  } yield ()

  def onlyForReg[F[_]: Concurrent: Timer](client: Client[F], userStateRef: Ref[F, UserState[F]]): F[Unit] =
    for {
      command <- Sync[F].delay{
        println("Write command. ('list')")
        val command = StdIn.readLine()
        println(s"Your command: ${command}.")
        command
      }
      _ <- if (command == "list")
              PrintChats[F](client, userStateRef).run(command.split(" ").tail.toList)
           else if (command.split(" ").head == "write")
              WriteSecure[F](client, userStateRef).run(command.split(" ").tail.toList)
           else if (command.split(" ").head == "createPrivateGroup") {
            Sync[F].delay(println("xyi")) >> {
              val handler = CreatePrivateGroup[F](client, userStateRef)
              handler.run(command.split(" ").tail.toList)
            }
          } else if (command.split(" ").head == "read") {
              ReadChat[F](client, userStateRef).run(command.split(" ").tail.toList)
          } else if (command.split(" ").head == "writeSecure") {
              WriteSecure[F](client, userStateRef).run(command.split(" ").tail.toList)
          } else (Sync[F].delay(println(s"Receive unkown command: ${command}")))
        _ <- onlyForReg(client, userStateRef)
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

  override def run(args: List[String]): IO[ExitCode] = anotherProg.compile.drain.as(ExitCode.Success)
}
