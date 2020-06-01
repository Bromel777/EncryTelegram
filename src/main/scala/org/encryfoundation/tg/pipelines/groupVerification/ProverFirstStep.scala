package org.encryfoundation.tg.pipelines.groupVerification

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import it.unisa.dia.gas.jpbc.Element
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi.SecretChat
import org.encryfoundation.mitmImun.Prover
import org.encryfoundation.tg.RunApp.sendMessage
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.services.PrivateConferenceService
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base64
import scorex.crypto.hash.Blake2b256
import cats.implicits._
import org.encryfoundation.tg.community.PrivateCommunity

case class ProverFirstStep[F[_]: Concurrent: Timer](prover: Prover,
                                                    community: PrivateCommunity,
                                                    recipientLogin: String,
                                                    userState: Ref[F, UserState[F]],
                                                    client: Client[F],
                                                    secretChat: SecretChat,
                                                    chatId: Long) extends Pipeline[F] {

  private def send2Chat(msg: Array[Byte]): F[Unit] =
    sendMessage(
      chatId,
      Base64.encode(msg),
      client
    )

  override def processInput(input: Array[Byte]): F[Pipeline[F]] = for {
    _ <- send2Chat(ProverFirstStep.pipeLineStart)
    firstStep <- Sync[F].delay(prover.firstStep())
    _ <- send2Chat(firstStep.toBytes)
    _ <- firstStepInfo(firstStep).traverse(send2Chat)
    _ <- send2Chat(ProverFirstStep.pipeLineEnd)
  } yield ProverSecondStep(
    prover,
    community,
    recipientLogin,
    userState,
    client,
    secretChat,
    chatId,
    firstStep
  )

  private def firstStepInfo(firstStep: Element): List[Array[Byte]] =
    List(
      firstStep.toBytes,
      community.gTilda.toBytes,
      prover.publicKey1.toBytes,
      prover.publicKey2.toBytes,
      prover.generator1.toBytes,
      prover.generator2.toBytes,
      prover.zRGenerator.toBytes
    )
}

object ProverFirstStep {
  val pipeLineStart = Blake2b256.hash("ProverFirstStepStart")
  val pipeLineEnd = Blake2b256.hash("ProverFirstStepEnd")
}
