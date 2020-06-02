package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer

import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg

trait StepMsgSerializer[M <: StepMsg] {
  def toBytes(msg: M): Array[Byte]
  def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, M]
}

object StepMsgSerializer {
  def toBytes[M <: StepMsg](msg: M)(implicit serializer: StepMsgSerializer[M]): Array[Byte] =
    serializer.toBytes(msg)

  def parseBytes[M <: StepMsg](bytes: Array[Byte])
                              (implicit serializer: StepMsgSerializer[M]): Either[StepMsgSerializationError, M] =
    serializer.parseBytes(bytes)
}
