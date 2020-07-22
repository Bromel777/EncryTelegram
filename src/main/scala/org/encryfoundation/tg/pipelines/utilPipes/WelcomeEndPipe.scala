package org.encryfoundation.tg.pipelines.utilPipes

import cats.Applicative
import cats.effect.{Concurrent, Timer}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.pipelines.messages.StepMsg.WelcomeMsg.WelcomeResponseMsg
import org.encryfoundation.tg.pipelines.messages.serializer.StepMsgSerializer
import org.encryfoundation.tg.services.{ClientService, UserStateService}
import org.encryfoundation.tg.pipelines.messages.serializer.utilsMsg.WelcomeMsgSerializer._
import scorex.crypto.encode.Base64
import scorex.crypto.hash.Blake2b256

case class WelcomeEndPipe[F[_]: Concurrent: Timer: Logger](chatId: Long,
                                                           mainPipeline: Pipeline[F])
                                                          (userStateService: UserStateService[F],
                                                           clientService: ClientService[F]) extends Pipeline[F] {
  //todo: err
  override def processInput(input: Array[Byte]): F[Pipeline[F]] =
    StepMsgSerializer.parseBytes(input) match {
      case Left(err) =>
        Logger[F].error(s"Err during parsing step: ${err}. Input: ${input.map(_.toChar).mkString}") >> Applicative[F].pure(this)
      case Right(WelcomeResponseMsg(hash)) =>
        if (hash sameElements Blake2b256.hash(WelcomeInitPipe.welcomeMsgText))
          mainPipeline.processInput(Array.emptyByteArray)
        else
          Logger[F].error(s"Err during verification welcome response. ChatId: ${chatId}." +
            s" Hash should be: ${Base64.encode(Blake2b256.hash(WelcomeInitPipe.welcomeMsgText))}." +
            s" Get: ${Base64.encode(hash)}") >> Applicative[F].pure(this)
    }
}
