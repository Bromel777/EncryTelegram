package org.encryfoundation.tg.pipelines

import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg

trait Pipeline[F[_]] {

  def processInput(input: Array[Byte]): F[Pipeline[F]]
}

trait HeadPipeline[F[_]] extends Pipeline[F] {
  val pipelineName: String
}
