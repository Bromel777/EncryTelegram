package org.encryfoundation.tg.pipelines.groupVerification

import cats.Applicative
import cats.effect.concurrent.{MVar, Ref}
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
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base64
import scorex.crypto.hash.Blake2b256

case class VerifierForthStep[F[_]: Concurrent: Timer: Logger](verifier: Verifier,
                                                              firstStep: Element,
                                                              secondStep: Element,
                                                              thirdStep: MVar[F, ProverThirdStepMsg],
                                                              client: Client[F],
                                                              stateRef: Ref[F, UserState[F]],
                                                              chatId: Long) extends Pipeline[F] {

  def processPreviousStepStart: F[Pipeline[F]] = Applicative[F].pure(this)

  def processPreviousStepEnd: F[Pipeline[F]] = for {
    state <- stateRef.get
    thirdStepMsg <- thirdStep.read
    result <- verifier.forthStep(
      firstStep,
      secondStep,
      thirdStepMsg.thirdStep
    ).pure[F]
    _ <- Logger[F].info(s"Verification res: ${result}")
    commonKey <- verifier.produceCommonKey(
      firstStep,
      secondStep,
      verifier.RoI1,
      verifier.RoI2
    ).pure[F]
    aes <- AESEncryption(Blake2b256.hash(commonKey)).pure[F]
    _ <- Logger[F].info(s"ChatId: ${thirdStepMsg.chatId}")
    _ <- Logger[F].info(s"Chats: ${state.mainChatList}")
    chat <- state.mainChatList.find(_._2.id == thirdStepMsg.chatId).get._2.pure[F]
    _ <- stateRef.update(_.copy(
      privateGroups = state.privateGroups +
        (chat.id -> (chat, aes.decrypt(Base64.decode(thirdStepMsg.pass).get).map(_.toChar).mkString)))
    )
    _ <- Logger[F].info(s"common key: ${Base64.encode(commonKey)}")
    _ <- Logger[F].info(s"Group name: ${aes.decrypt(Base64.decode(thirdStepMsg.name).get).map(_.toChar).mkString}." +
      s" Pass: ${aes.decrypt(Base64.decode(thirdStepMsg.pass).get).map(_.toChar).mkString}")
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
