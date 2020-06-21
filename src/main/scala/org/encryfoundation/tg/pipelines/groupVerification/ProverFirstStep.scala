package org.encryfoundation.tg.pipelines.groupVerification

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.{MVar, Ref}
import it.unisa.dia.gas.jpbc.Element
import org.drinkless.tdlib.{Client, ClientUtils, TdApi}
import org.drinkless.tdlib.TdApi.SecretChat
import org.encryfoundation.mitmImun.{Prover, Verifier}
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base64
import scorex.crypto.hash.Blake2b256
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.GroupVerificationStepMsg.{ProverFirstStepMsg, VerifierSecondStepMsg}
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.{EndPipeline, StartPipeline}
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StartPipelineMsgSerializer._
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.EndPipelineMsgSerializer._
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.groupVerification.ProverFirstMsgSerializer._
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializer
import org.encryfoundation.tg.services.PrivateConferenceService

class ProverFirstStep[F[_]: Concurrent: Timer: Logger] private(prover: Prover,
                                                               community: PrivateCommunity,
                                                               recipientLogin: String,
                                                               chatPass: String,
                                                               userState: Ref[F, UserState[F]],
                                                               client: Client[F],
                                                               chat: TdApi.Chat,
                                                               chatId: Long) extends Pipeline[F] {

  private def send2Chat[M <: StepMsg](msg: M)(implicit s: StepMsgSerializer[M]): F[Unit] = for {
    _ <- Logger[F].info(s"Send : ${msg}")
    _ <- ClientUtils.sendMessage(
      chatId,
      Base64.encode(StepMsgSerializer.toBytes(msg)),
      client
    )
  } yield ()


  override def processInput(input: Array[Byte]): F[Pipeline[F]] = for {
    msg <- getFirstMsg.pure[F]
    _ <- send2Chat(StartPipeline(ProverFirstStep.pipelineName))
    _ <- send2Chat(msg)
    _ <- send2Chat(EndPipeline(ProverFirstStep.pipelineName))
    emptyMvar <- MVar.empty[F, VerifierSecondStepMsg]
  } yield ProverThirdStep(
    prover,
    community,
    recipientLogin,
    chatPass,
    userState,
    client,
    chat,
    chatId,
    msg.firstStep,
    emptyMvar
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

  def apply[F[_]: Concurrent: Timer: Logger](client: Client[F],
                                             userState: Ref[F, UserState[F]],
                                             confName: String,
                                             recipientLogin: String,
                                             chatPass: String,
                                             chat: TdApi.Chat,
                                             chatId: Long)(privConfService: PrivateConferenceService[F]): F[ProverFirstStep[F]] =
    for {
      pairing <- Sync[F].delay(PairingFactory.getPairing("src/main/resources/properties/a.properties"))
      groupInfo <- privConfService.findConf(confName)
      prover <- Prover(
        groupInfo.G1Gen,
        groupInfo.G2Gen,
        groupInfo.users.head.userData.userKsi,
        groupInfo.users.head.userData.userT,
        groupInfo.users.head.userData.publicKey1,
        groupInfo.users.head.userData.publicKey2,
        groupInfo.ZrGen,
        pairing
      ).pure[F]
    } yield new ProverFirstStep(
      prover,
      groupInfo,
      recipientLogin,
      chatPass,
      userState,
      client,
      chat,
      chatId
    )

  val pipelineName = "proverFirstStep"
}
