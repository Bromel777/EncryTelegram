package org.encryfoundation.tg.pipelines.groupVerification

import cats.Applicative
import cats.effect.concurrent.MVar
import cats.effect.{Concurrent, Timer}
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.GroupVerificationStepMsg.ProverThirdStepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.{EndPipeline, StartPipeline}
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializer
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import it.unisa.dia.gas.jpbc.Element
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi.SecretChat
import org.encryfoundation.mitmImun.Verifier

case class VerifierForthStep[F[_]: Concurrent: Timer: Logger](verifier: Verifier,
                                                              firstStep: Element,
                                                              secondStep: Element,
                                                              thirdStep: MVar[F, ProverThirdStepMsg],
                                                              client: Client[F],
                                                              chatId: Long) extends Pipeline[F] {

  def processPreviousStepStart: F[Pipeline[F]] = Applicative[F].pure(this)

  def processPreviousStepEnd: F[Pipeline[F]] = for {
    thirdStepMsg <- thirdStep.read
    result <- verifier.forthStep(
      firstStep,
      secondStep,
      thirdStepMsg.thirdStep
    ).pure[F]
    _ <- Logger[F].info(s"Verification res: ${result}")
  } yield this

  def processStepInput(input: StepMsg): F[Pipeline[F]] = input match {
    case msg: ProverThirdStepMsg => for {
      _ <- thirdStep.put(msg)
    } yield this
    case _ => Applicative[F].pure(this)
  }

  override def processInput(input: Array[Byte]): F[Pipeline[F]] =
    StepMsgSerializer.parseBytes(input).right.get match {
      case StartPipeline(pipelineName) if pipelineName == ProverThirdStep.pipelineName  => processPreviousStepStart
      case EndPipeline(pipelineName) if pipelineName == ProverThirdStep.pipelineName => processPreviousStepEnd
      case msg: ProverThirdStepMsg => processStepInput(msg)
    }
}
