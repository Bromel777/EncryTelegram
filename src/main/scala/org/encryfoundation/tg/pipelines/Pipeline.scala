package org.encryfoundation.tg.pipelines

import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg

trait Pipeline[F[_]] {

  def processInput(input: StepMsg): F[Pipeline[F]]
}
