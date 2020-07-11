package org.encryfoundation.tg.services

import cats.Applicative
import cats.effect.concurrent.{MVar, Ref}
import cats.effect.{Concurrent, ConcurrentEffect, IO, Timer}
import org.drinkless.tdlib.{Client, ResultHandler, TdApi}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.handlers.{EmptyHandler, SuccessHandler}
import org.drinkless.tdlib.Client._
import org.encryfoundation.tg.Handler
import org.encryfoundation.tg.userState.UserState
import fs2.Stream
import org.encryfoundation.tg.services.ClientService.ClientStep.{Logout, Normal}
import org.encryfoundation.tg.services.ClientService.Live

trait ClientService[F[_]] {
  def sendRequest(func: TdApi.Function, handler: ResultHandler[F]): F[Unit]
  def sendRequest(func: TdApi.Function): F[Unit]
  def updateClient(client: Client[F]): F[Unit]
  def runClient(): Stream[F, Unit]
  def logout(): F[Unit]
}

object ClientService {

  sealed trait ClientStep
  object ClientStep {
    case object Logout extends ClientStep
    case object Normal extends ClientStep
  }

  final private class Live[F[_]: ConcurrentEffect: Timer: Logger](privateConferenceService: PrivateConferenceService[F],
                                                                  userStateService: UserStateService[F],
                                                                  userStateRef: Ref[F, UserState[F]],
                                                                  currentStep: Ref[F, ClientStep],
                                                                  clientRef: MVar[F, Client[F]]) extends ClientService[F] {

    override def sendRequest(func: TdApi.Function, handler: ResultHandler[F]): F[Unit] =
      clientRef.isEmpty.flatMap { isEmpty =>
        if (isEmpty) Applicative[F].pure(())
        else clientRef.read.flatMap(_.send(func, handler))
      }

    override def sendRequest(func: TdApi.Function): F[Unit] = func match {
      case _: TdApi.OpenChat => checkForSuccess(func)
      case _ => sendRequest(func, EmptyHandler[F]())
    }

    override def updateClient(client: Client[F]): F[Unit] = clientRef.put(client)

    override def logout(): F[Unit] = currentStep.set(Logout)

    override def runClient: Stream[F, Unit] =
      Stream.eval(currentStep.get)
        .repeat
        .flatMap {
          case ClientStep.Logout =>
            for {
              _ <- Stream.eval(createClient)
              _ <- Stream.eval(currentStep.set(Normal))
            } yield ()
          case ClientStep.Normal =>
            for {
              client <- Stream.eval(clientRef.read)
              _ <- Stream.eval(client.receiveQueries(300))
            } yield ()
        }

    private def createClient: F[Unit] =
      for {
        client <- Client[F](EmptyHandler[F])
        handler <- Handler[F](userStateRef, privateConferenceService, userStateService, this)
        _ <- client.setUpdatesHandler(handler)
        _ <- client.execute(new TdApi.SetLogVerbosityLevel(0))
        _ <- clientRef.tryTake
        _ <- clientRef.put(client)
      } yield ()

    private def checkForSuccess(func: TdApi.Function): F[Unit] =
      for {
        checkMVar <- MVar.empty[F, Boolean]
        _ <- sendRequest(func, SuccessHandler(checkMVar))
        _ <- checkMVar.read
      } yield ()
  }

  def apply[F[_]: ConcurrentEffect: Timer: Logger](privateConferenceService: PrivateConferenceService[F],
                                                   userStateService: UserStateService[F],
                                                   userStateRef: Ref[F, UserState[F]]): F[ClientService[F]] =
    for {
      client <- Client[F](EmptyHandler[F])
      clientMvar <- MVar.of(client)
      step <- Ref.of[F, ClientStep](Normal)
      live <- Applicative[F].pure(new Live(privateConferenceService, userStateService, userStateRef, step, clientMvar))
      handler <- Handler[F](userStateRef, privateConferenceService, userStateService, live)
      _ <- client.setUpdatesHandler(handler)
      _ <- client.execute(new TdApi.SetLogVerbosityLevel(0))
    } yield live
}


