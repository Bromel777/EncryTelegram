package org.encryfoundation.tg

import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.{Concurrent, ExitCode, IO, IOApp, Sync, Timer}
import cats.syntax.applicative._
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client, Client123, DummyHandler, TdApi}
import org.encryfoundation.tg.userState.UserState
import fs2.Stream
import cats.implicits._
import org.drinkless.tdlib.Client123.ResultHandler
import scala.concurrent.duration._

object RunApp extends IOApp {
  System.loadLibrary("tdjni")
  //Client.execute(new TdApi.SetLogVerbosityLevel(0))
  println("Hello!")

//  def workingCycle(stateRef: Ref[IO, UserState]): IO[Unit] = for {
//    state <- stateRef.get
//    _ <- if (state.isAuth) { state.client.send(new TdApi.GetChats(
//      new TdApi.ChatListMain,
//      Long.MaxValue,
//      0,
//      20),
//      new Client.ResultHandler {
//        /**
//         * Callback called on result of query to TDLib or incoming update from TDLib.
//         *
//         * @param obj Result of query or update of type TdApi.Update about new events.
//         */
//        override def onResult(obj: TdApi.Object): Unit = obj.getConstructor match {
//          case TdApi.Error.CONSTRUCTOR =>
//            println(s"Err occured. $obj")
//          case TdApi.Chats.CONSTRUCTOR =>
//            println(s"Get chats: ${obj.asInstanceOf[TdApi.Chats]}")
//          case _ =>
//            println("Any")
//            ()
//        }
//      })
//    }.pure[IO] else ().pure[IO]
//    _ <- workingCycle(stateRef)
//  } yield ()

  val program = for {
    queueRef <- Ref.of[IO, List[TdApi.Object]](List.empty)
    client <- Client[IO](EmptyHandler(queueRef))
    _ <- client.execute(new TdApi.SetLogVerbosityLevel(0))
    ref <- Ref.of[IO, UserState[IO]](UserState[IO](List.empty, Map.empty, false, client))
    handler <- Handler(ref, queueRef)
    _ <- client.setUpdatesHandler(handler)
  } yield (queueRef, client, ref)

  val anotherProg = Stream.eval(program).flatMap { case (queue, client, ref) =>
    client.run() concurrently Stream.eval(getChats(client, ref))
  }

  def getChats[F[_]: Concurrent: Timer](client: Client[F], userStateRef: Ref[F, UserState[F]]): F[Unit] = for {
    _ <- Sync[F].delay(println("Invoke!"))
    state <- userStateRef.get
    _ <- if (state.isAuth) client.send(
      new TdApi.GetChats(new TdApi.ChatListMain(), Long.MaxValue, 0, 20),
      DummyHandler[F](client, userStateRef, (cl, ref) => getChats(cl, ref))
    ) else Sync[F].pure(())
    _ <- Sync[F].delay(println(s"Chats: ${
      state.mainChatList.map(_.title).mkString("\n ")}"))
    _ <- Timer[F].sleep(5.seconds) >> getChats(client, userStateRef)
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = anotherProg.compile.drain.as(ExitCode.Success)
}
