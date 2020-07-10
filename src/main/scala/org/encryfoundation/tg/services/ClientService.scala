package org.encryfoundation.tg.services

import cats.Applicative
import cats.effect.concurrent.{MVar, Ref}
import cats.effect.{Concurrent, ConcurrentEffect, IO, Timer}
import org.drinkless.tdlib.{Client, ResultHandler, TdApi}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.handlers.EmptyHandler
import org.drinkless.tdlib.Client._
import org.encryfoundation.tg.Handler
import org.encryfoundation.tg.userState.UserState
import fs2.Stream
import org.encryfoundation.tg.services.ClientService.Live

trait ClientService[F[_]] {
  def sendRequest(func: TdApi.Function, handler: ResultHandler[F]): F[Unit]
  def sendRequest(func: TdApi.Function): F[Unit]
  def updateClient(client: Client[F]): F[Unit]
  def runClient(): Stream[F, Unit]
  def logout(): F[Unit]
}

object ClientService {

  final private class Live[F[_]: Concurrent: Timer: Logger](privateConferenceService: PrivateConferenceService[F],
                                                            userStateService: UserStateService[F],
                                                            userStateRef: Ref[F, UserState[F]],
                                                            clientRef: MVar[F, Client[F]]) extends ClientService[F] {

    override def sendRequest(func: TdApi.Function, handler: ResultHandler[F]): F[Unit] =
      clientRef.isEmpty.flatMap { isEmpty =>
        if (isEmpty) Applicative[F].pure(())
        else clientRef.read.flatMap(_.send(func, handler))
      }

    override def sendRequest(func: TdApi.Function): F[Unit] = sendRequest(func, EmptyHandler[F]())

    override def updateClient(client: Client[F]): F[Unit] = clientRef.put(client)

    override def logout(): F[Unit] = clientRef.tryTake >> ().pure[F]

    override def runClient: Stream[F, Unit] = for {
      client <- Stream.eval(clientRef.read)
      stream <- client.run()
    } yield stream

  }

  def apply[F[_]: ConcurrentEffect: Timer: Logger](privateConferenceService: PrivateConferenceService[F],
                                                   userStateService: UserStateService[F],
                                                   userStateRef: Ref[F, UserState[F]]): F[ClientService[F]] =
    for {
      client <- Client[F](EmptyHandler[F])
      clientMvar <- MVar.of(client)
      live <- Applicative[F].pure(new Live(privateConferenceService, userStateService, userStateRef, clientMvar))
      handler <- Handler[F](userStateRef, privateConferenceService, userStateService, live)
      _ <- client.setUpdatesHandler(handler)
      _ <- client.execute(new TdApi.SetLogVerbosityLevel(0))
    } yield live
}


