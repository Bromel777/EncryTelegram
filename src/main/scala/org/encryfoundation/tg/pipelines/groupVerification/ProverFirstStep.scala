package org.encryfoundation.tg.pipelines.groupVerification

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import it.unisa.dia.gas.jpbc.Element
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi.SecretChat
import org.encryfoundation.mitmImun.Prover
import org.encryfoundation.tg.RunApp.sendMessage
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base64
import scorex.crypto.hash.Blake2b256
import cats.implicits._
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.GroupVerificationStepMsg.ProverFirstStepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.{EndPipeline, StartPipeline}
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StartPipelineMsgSerializer._
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.EndPipelineMsgSerializer._
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.groupVerification.ProverFirstMsgSerializer._
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializer

case class ProverFirstStep[F[_]: Concurrent: Timer](prover: Prover,
                                                    community: PrivateCommunity,
                                                    recipientLogin: String,
                                                    userState: Ref[F, UserState[F]],
                                                    client: Client[F],
                                                    secretChat: SecretChat,
                                                    chatId: Long) extends Pipeline[F] {

  private def send2Chat[M <: StepMsg](msg: M)(implicit s: StepMsgSerializer[M]): F[Unit] =
    sendMessage(
      chatId,
      Base64.encode(StepMsgSerializer.toBytes(msg)),
      client
    )

  override def processInput(input: StepMsg): F[Pipeline[F]] = for {
    msg <- getFirstMsg.pure[F]
    _ <- send2Chat(StartPipeline(ProverFirstStep.pipelineName))
    _ <- send2Chat(msg)
    _ <- send2Chat(EndPipeline(ProverFirstStep.pipelineName))
  } yield ProverThirdStep(
    prover,
    community,
    recipientLogin,
    userState,
    client,
    secretChat,
    chatId,
    msg.firstStep
  )

  private def getFirstMsg: ProverFirstStepMsg =
    ProverFirstStepMsg(
      prover.firstStep(),
      community.gTilda,
      prover.publicKey1,
      prover.publicKey2,
      prover.generator1,
      prover.generator2,
      prover.zRGenerator
    )
}

object ProverFirstStep {
  val pipelineName = "proverFirstStep"
}
