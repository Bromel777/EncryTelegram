package org.encryfoundation.tg.pipelines.utilPipes

import cats.Applicative
import cats.effect.{Concurrent, Timer}
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.ClientUtils
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.services.{ClientService, UserStateService}
import org.encryfoundation.tg.utils.ChatUtils
import scorex.crypto.encode.Base16
import scorex.crypto.hash.Blake2b256
import cats.implicits._

case class WelcomeProcessPipe[F[_]: Concurrent: Timer: Logger](chatId: Long)
                                                              (userStateService: UserStateService[F],
                                                               clientService: ClientService[F]) extends Pipeline[F] {

  override def processInput(input: Array[Byte]): F[Pipeline[F]] =
    ClientUtils.sendMessage(chatId, Base16.encode(Blake2b256.hash(chatId.toString)), clientService).map { _ =>
      EmptyPipeline(chatId)(userStateService, clientService)
    }
}

object WelcomeProcessPipe {
  def startPipeline[F[_]: Concurrent: Timer: Logger](chatId: Long)
                                                    (userStateService: UserStateService[F],
                                                     clientService: ClientService[F]): F[Unit] = {
    val pipeline = WelcomeProcessPipe(chatId)(userStateService, clientService)
    pipeline.processInput(Array.emptyByteArray) >> Applicative[F].unit
  }
}
