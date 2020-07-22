package org.encryfoundation.tg.pipelines.utilPipes

import cats.Applicative
import cats.effect.{Concurrent, Timer}
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.ClientUtils
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.services.{ClientService, UserStateService}
import org.encryfoundation.tg.utils.ChatUtils
import scorex.crypto.encode.{Base16, Base64}
import scorex.crypto.hash.Blake2b256
import cats.implicits._
import org.encryfoundation.tg.pipelines.messages.StepMsg
import org.encryfoundation.tg.pipelines.messages.StepMsg.WelcomeMsg.WelcomeResponseMsg
import org.encryfoundation.tg.pipelines.messages.serializer.StepMsgSerializer
import org.encryfoundation.tg.pipelines.messages.serializer.utilsMsg.WelcomeMsgSerializer._

case class WelcomeProcessPipe[F[_]: Concurrent: Timer: Logger](chatId: Long)
                                                              (userStateService: UserStateService[F],
                                                               clientService: ClientService[F]) extends Pipeline[F] {

  private def send2Chat[M <: StepMsg](msg: M)(implicit s: StepMsgSerializer[M]): F[Unit] = for {
    _ <- Logger[F].info(s"Send to chat with id (${chatId}) : ${msg}")
    _ <- ClientUtils.sendMessage(
      chatId,
      Base64.encode(StepMsgSerializer.toBytes(msg)),
      clientService
    )
  } yield ()

  override def processInput(input: Array[Byte]): F[Pipeline[F]] = {
    Logger[F].info(s"Send msg with hash: ${Base64.encode(Blake2b256.hash(WelcomeInitPipe.welcomeMsgText))}") >>
    send2Chat(WelcomeResponseMsg(Blake2b256.hash(WelcomeInitPipe.welcomeMsgText))).map { _ =>
      EmptyPipeline(chatId)(userStateService, clientService)
    }
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
