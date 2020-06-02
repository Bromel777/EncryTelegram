package org.encryfoundation.tg.pipelines.groupVerification

import org.encryfoundation.tg.pipelines.Pipeline

case class VerifierForthStep[F[_]]() extends Pipeline[F] {

  override def processInput(input: Array[Byte]): F[Pipeline[F]] = ???
}
