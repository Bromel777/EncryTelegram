package org.encryfoundation.tg.pipelines.groupVerification

import cats.Applicative
import cats.effect.concurrent.{MVar, Ref}
import cats.effect.{Concurrent, Timer}
import cats.implicits._
import it.unisa.dia.gas.jpbc.Element
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.mitmImun.Prover
import org.encryfoundation.tg.RunApp.sendMessage
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.GroupVerificationStepMsg.{ProverThirdStepMsg, VerifierSecondStepMsg}
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.{EndPipeline, StartPipeline}
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.EndPipelineMsgSerializer._
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StartPipelineMsgSerializer._
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializer
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.groupVerification.ProverThirdMsgSerializer._
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base64

case class ProverThirdStep[F[_]: Concurrent: Timer](prover: Prover,
                                                    community: PrivateCommunity,
                                                    recipientLogin: String,
                                                    chatPass: String,
                                                    userState: Ref[F, UserState[F]],
                                                    client: Client[F],
                                                    secretChat: TdApi.Chat,
                                                    chatId: Long,
                                                    firstStep: Element,
                                                    verifierSecondStepMsg: MVar[F, VerifierSecondStepMsg]) extends Pipeline[F] {

  private def send2Chat[M <: StepMsg](msg: M)(implicit s: StepMsgSerializer[M]): F[Unit] =
    sendMessage(
      chatId,
      Base64.encode(StepMsgSerializer.toBytes(msg)),
      client
    )

  def processPreviousStepStart: F[Pipeline[F]] = Applicative[F].pure(this)

  def processPreviousStepEnd: F[Pipeline[F]] = for {
    _ <- send2Chat(StartPipeline(ProverThirdStep.pipelineName))
    secondStep <- verifierSecondStepMsg.read
    thirdStep <- prover.thirdStep(secondStep.secondStep).pure[F]
    commonKey <- prover.produceCommonKey(secondStep.verifierPubKey1, firstStep, secondStep.secondStep).pure[F]
    aes <- AESEncryption(commonKey).pure[F]
    _ <- send2Chat(
      ProverThirdStepMsg(
        thirdStep,
        chatId,
        Base64.encode(aes.encrypt(community.name.getBytes)),
        Base64.encode(aes.encrypt(chatPass.getBytes))
      )
    )
    _ <- send2Chat(EndPipeline(ProverThirdStep.pipelineName))
  } yield this

  def processStepInput(input: StepMsg): F[Pipeline[F]] = input match {
    case msg: VerifierSecondStepMsg =>
      for {
        _ <- verifierSecondStepMsg.put(msg)
      } yield this
    case _ => Applicative[F].pure(this)
  }

  override def processInput(input: Array[Byte]): F[Pipeline[F]] =
    StepMsgSerializer.parseBytes(input).right.get match {
      case StartPipeline(pipelineName) if pipelineName == VerifierSecondStep.pipeLineName  => processPreviousStepStart
      case EndPipeline(pipelineName) if pipelineName == VerifierSecondStep.pipeLineName => processPreviousStepEnd
      case msg: VerifierSecondStepMsg => processStepInput(msg)
    }
}

object ProverThirdStep {
  val pipelineName = "proverThirdStep"
}
