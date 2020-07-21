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
import scorex.crypto.hash.Blake2b256

case class WelcomeEndPipe[F[_]: Concurrent: Timer: Logger](chatId: Long,
                                                           mainPipeline: Pipeline[F])
                                                          (userStateService: UserStateService[F],
                                                           clientService: ClientService[F]) extends Pipeline[F] {
  //todo: err
  override def processInput(input: Array[Byte]): F[Pipeline[F]] =
    StepMsgSerializer.parseMsgBytes[WelcomeResponseMsg](input) match {
      case Left(err) =>
        Logger[F].error(s"Err during parsing step: ${err}. Input: ${input.map(_.toChar).mkString}") >> Applicative[F].pure(this)
      case Right(value) =>
        if (value.msgHash sameElements Blake2b256.hash(chatId.toString))
          mainPipeline.processInput(Array.emptyByteArray)
        else
          Logger[F].error(s"Err during verification welcome response") >> Applicative[F].pure(this)
    }
}
