package org.encryfoundation.tg.pipelines.groupVerification

import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg

case class VerifierForthStep[F[_]]() extends Pipeline[F] {

  override def processInput(input: StepMsg): F[Pipeline[F]] = ???
}
