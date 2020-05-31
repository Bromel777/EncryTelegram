package org.encryfoundation.tg.community

import it.unisa.dia.gas.jpbc.Element
import org.encryfoundation.tg.mitm.{Prover, Verifier}

trait InviteStatus {
  def setStepValue(input: Array[Byte]): InviteStatus
}

object InviteStatus {
  case class AwaitingFirstStep() extends InviteStatus {
    override def setStepValue(input: Array[Byte]): InviteStatus = AwaitingGroupKey(input)
  }
  case class AwaitingGroupKey(firstStep: Array[Byte]) extends InviteStatus {
    override def setStepValue(input: Array[Byte]): InviteStatus =
      AwaitingProverFirstPublicKey(
        firstStep,
        input
      )
  }
  case class AwaitingProverFirstPublicKey(firstStepBytes: Array[Byte],
                                          gTildaBytes: Array[Byte]) extends InviteStatus {
    override def setStepValue(input: Array[Byte]): InviteStatus =
      AwaitingProverSecondKey(
        firstStepBytes,
        gTildaBytes,
        input
      )
  }
  case class AwaitingProverSecondKey(firstStepBytes: Array[Byte],
                                     gTildaBytes: Array[Byte],
                                     firstPublicKeyBytes: Array[Byte]) extends InviteStatus {
    override def setStepValue(input: Array[Byte]): AwaitingG1Gen =
      AwaitingG1Gen(
        firstStepBytes,
        gTildaBytes,
        firstPublicKeyBytes,
        input
      )
  }

  case class AwaitingG1Gen(firstStepBytes: Array[Byte],
                           gTildaBytes: Array[Byte],
                           firstPublicKeyBytes: Array[Byte],
                           secondPublicKeyBytes: Array[Byte]) extends InviteStatus {
    override def setStepValue(input: Array[Byte]): AwaitingG2Gen =
      AwaitingG2Gen(
        firstStepBytes,
        gTildaBytes,
        firstPublicKeyBytes,
        secondPublicKeyBytes,
        input
      )
  }

  case class AwaitingG2Gen(firstStepBytes: Array[Byte],
                           gTildaBytes: Array[Byte],
                           firstPublicKeyBytes: Array[Byte],
                           secondPublicKeyBytes: Array[Byte],
                           g1GenBytes: Array[Byte]) extends InviteStatus {
    override def setStepValue(input: Array[Byte]): AwaitingZrGen =
      AwaitingZrGen(
        firstStepBytes,
        gTildaBytes,
        firstPublicKeyBytes,
        secondPublicKeyBytes,
        g1GenBytes,
        input
      )
  }

  case class AwaitingZrGen(firstStepBytes: Array[Byte],
                           gTildaBytes: Array[Byte],
                           firstPublicKeyBytes: Array[Byte],
                           secondPublicKeyBytes: Array[Byte],
                           g1GenBytes: Array[Byte],
                           g2GenBytes: Array[Byte]) extends InviteStatus {
    override def setStepValue(input: Array[Byte]): InviteStatus =
      CompleteFirstStep(
        firstStepBytes,
        gTildaBytes,
        firstPublicKeyBytes,
        secondPublicKeyBytes,
        g1GenBytes,
        g2GenBytes,
        input
      )
  }

  case class CompleteFirstStep(firstStepBytes: Array[Byte],
                               gTildaBytes: Array[Byte],
                               firstPublicKeyBytes: Array[Byte],
                               secondPublicKeyBytes: Array[Byte],
                               g1GenBytes: Array[Byte],
                               g2GenBytes: Array[Byte],
                               zRGenBytes: Array[Byte]) extends InviteStatus {
    override def setStepValue(input: Array[Byte]): InviteStatus = this
  }

  case class VerifierSecondStep(verifier: Verifier,
                                firstStepBytes: Array[Byte],
                                secondStepBytes: Array[Byte],
                                canProcess: Boolean = false) extends InviteStatus {
    override def setStepValue(input: Array[Byte]): InviteStatus =
      if (canProcess) VerifierThirdStep(
        verifier,
        firstStepBytes,
        secondStepBytes,
        input
      ) else this
  }

  case class VerifierThirdStep(verifier: Verifier,
                               firstStepBytes: Array[Byte],
                               secondStepBytes: Array[Byte],
                               thirdStepBytes: Array[Byte]) extends InviteStatus {
    override def setStepValue(input: Array[Byte]): InviteStatus = this.copy(thirdStepBytes = input)
  }

  case class ProverSecondStep(prover: Prover, firstStep: Element) extends InviteStatus {
    override def setStepValue(input: Array[Byte]): InviteStatus = this
  }

  case class ProverThirdStep(prover: Prover, firstStep: Array[Byte], secondStepByte: Array[Byte]) extends InviteStatus {
    override def setStepValue(input: Array[Byte]): InviteStatus = this
  }

  case class ProverFirstStep(prover: Prover, firstStep: Array[Byte], canProcess: Boolean = false) extends InviteStatus {
    override def setStepValue(input: Array[Byte]): InviteStatus =
      if (canProcess) ProverThirdStep(
        prover,
        firstStep,
        input
      ) else this
  }
}
