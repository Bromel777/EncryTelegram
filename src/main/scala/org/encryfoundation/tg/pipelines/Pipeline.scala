package org.encryfoundation.tg.pipelines

trait Pipeline[F[_]] {

  def processInput(input: Array[Byte]): F[Pipeline[F]]
}
