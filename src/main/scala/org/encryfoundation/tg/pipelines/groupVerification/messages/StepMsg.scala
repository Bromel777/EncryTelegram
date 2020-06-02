package org.encryfoundation.tg.pipelines.groupVerification.messages

import it.unisa.dia.gas.jpbc.Element

sealed trait StepMsg
object StepMsg {
  case class StartPipeline(pipelineName: String) extends StepMsg
  case class EndPipeline(pipelineName: String) extends StepMsg
  case class ProverFirstStepMsg(firstStep: Element,
                                gTilda: Element,
                                proverPublicKey1: Element,
                                proverPublicKey2: Element,
                                g1Gen: Element,
                                g2Gen: Element,
                                zRGen: Element) extends StepMsg

  case class VerifierSecondStepMsg(verifierPubKey1: Element,
                                secondStep: Element) extends StepMsg

  case class ProverThirdStepMsg(thirdStep: Array[Byte]) extends StepMsg
}