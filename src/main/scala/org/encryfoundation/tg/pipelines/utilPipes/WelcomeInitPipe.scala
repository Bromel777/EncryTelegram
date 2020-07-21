package org.encryfoundation.tg.pipelines.utilPipes

import cats.effect.{Concurrent, Timer}
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{ClientUtils, TdApi}
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.pipelines.messages.StepMsg
import org.encryfoundation.tg.pipelines.messages.serializer.StepMsgSerializer
import org.encryfoundation.tg.services.{ClientService, UserStateService}
import scorex.crypto.encode.Base64
import cats.implicits._

class WelcomeInitPipe[F[_]: Concurrent: Timer: Logger](chatId: Long,
                                                       mainPipeline: Pipeline[F])
                                                      (userStateService: UserStateService[F],
                                                       clientService: ClientService[F]) extends Pipeline[F] {

  override def processInput(input: Array[Byte]): F[Pipeline[F]] =
    ClientUtils.sendMessage(chatId, WelcomeInitPipe.welcomeMsgText, clientService).map(_ =>
      WelcomeEndPipe(chatId, mainPipeline)(userStateService, clientService)
    )

}

object WelcomeInitPipe {

  val welcomeMsgText: String = "Hello! I am trying to start pipeline with you," +
    " but looks like you are not using Encry Telegram client. Please install it." +
    " https://github.com/EncryFoundation/EncryTelegram/releases"

  val pipelineName = "welcomePipe"
}
