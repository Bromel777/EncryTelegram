package org.encryfoundation.tg.pipelines.groupVerification

import cats.Applicative
import cats.effect.{Concurrent, Sync}
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi.SecretChat
import org.encryfoundation.mitmImun.{Prover, Verifier}
import org.encryfoundation.tg.RunApp.sendMessage
import org.encryfoundation.tg.pipelines.Pipeline
import scorex.crypto.encode.Base64
import scorex.crypto.hash.Blake2b256
import cats.implicits._
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.tg.pipelines.groupVerification.VerifierSecondStep.ProverInfo

case class VerifierSecondStep[F[_]: Concurrent: Logger](proverInfo: ProverInfo[F],
                                                        secretChat: SecretChat,
                                                        chatId: Long,
                                                        client: Client[F]) extends Pipeline[F] {

  private def send2Chat(msg: Array[Byte]): F[Unit] =
    sendMessage(
      chatId,
      Base64.encode(msg),
      client
    )

  def processPreviousStepStart: F[Pipeline[F]] = ???

  def processPreviousStepEnd: F[Pipeline[F]] = ???

  def processStepInput(input: Array[Byte]): F[Pipeline[F]] = for {
    newProverInfo <- proverInfo.setNextStep(input)
  } yield this.copy(proverInfo = newProverInfo)

  override def processInput(input: Array[Byte]): F[Pipeline[F]] = input match {
    case prevStepStart if prevStepStart sameElements ProverFirstStep.pipeLineStart => processPreviousStepStart
    case prevStepEnd if prevStepEnd sameElements ProverFirstStep.pipeLineEnd => processPreviousStepEnd
    case stepInput => processStepInput(input)
  }
}

object VerifierSecondStep {

  //todo: Add errors
  case class ProverInfo[F[_]: Sync](steps: List[Array[Byte]]) {
    def setNextStep(input: Array[Byte]): F[ProverInfo[F]] =
      if (steps.length < 7) this.copy[F](steps :+ input).pure[F]
      else Applicative[F].pure(this)
    def toVerifier: F[Verifier] = ???
  }

  val pipeLineStart = Blake2b256.hash("VerifierSecondStepStart")
  val pipeLineEnd = Blake2b256.hash("VerifierSecondStepEnd")
}
