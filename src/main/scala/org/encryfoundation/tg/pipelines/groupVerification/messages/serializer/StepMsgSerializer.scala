package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer

import EndPipelineProto.EndPipelineProtoMsg
import GroupVerificationProto.GroupVerificationProtoMsg
import StartPipelineProto.StartPipelineProtoMsg
import StepProto.StepMsgProto
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializationError.CorruptedBytes

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

  def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, StepMsg] = {
    val msg = StepMsgProto.parseFrom(bytes)
    if (msg.stepMsg.isStart) StartPipelineMsgSerializer.serializer.parseBytes(bytes)
    else if (msg.stepMsg.isEnd) EndPipelineMsgSerializer.serializer.parseBytes(bytes)
    else if (msg.stepMsg.isVerification)
    else Left[StepMsgSerializationError, StepMsg](CorruptedBytes)
  }
}
