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
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.mitmImun.Verifier
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.handlers.CloseChatHandler
import org.encryfoundation.tg.services.UserStateService
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base64
import scorex.crypto.hash.Blake2b256

case class VerifierForthStep[F[_]: Concurrent: Timer: Logger](verifier: Verifier,
                                                              firstStep: Element,
                                                              secondStep: Element,
                                                              thirdStep: MVar[F, ProverThirdStepMsg],
                                                              client: Client[F],
                                                              stateRef: Ref[F, UserState[F]],
                                                              chatId: Long)
                                                             (userStateService: UserStateService[F]) extends Pipeline[F] {

  def processPreviousStepStart: F[Pipeline[F]] = Applicative[F].pure(this)

  def processPreviousStepEnd: F[Pipeline[F]] = for {
    thirdStepMsg <- thirdStep.read
    result <- verifier.forthStep(
      firstStep,
      secondStep,
      thirdStepMsg.thirdStep
    ).pure[F]
    _ <- if (result) {
      for {
        commonKey <- verifier.produceCommonKey(
          firstStep,
          secondStep,
          verifier.RoI1,
          verifier.RoI2
        ).pure[F]
        aes <- AESEncryption(Blake2b256.hash(commonKey)).pure[F]
        decypherdGroup <- thirdStepMsg.privateGroupChat.copy(
          groupName = aes.decrypt(Base64.decode(thirdStepMsg.privateGroupChat.groupName).get).map(_.toChar).mkString,
          communityName = aes.decrypt(Base64.decode(thirdStepMsg.privateGroupChat.communityName).get).map(_.toChar).mkString,
          password = aes.decrypt(Base64.decode(thirdStepMsg.privateGroupChat.password).get).map(_.toChar).mkString
        ).pure[F]
        _ <- userStateService.persistPrivateGroupChat(decypherdGroup)
      } yield ()
    } else ().pure[F]
    _ <- client.send(new TdApi.CloseChat(chatId), CloseChatHandler[F](stateRef, client, chatId))
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
