package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer

import EndPipelineProto.EndPipelineProtoMsg
import GroupVerificationProto.GroupVerificationProtoMsg
import StepProto.StepMsgProto
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.EndPipeline

object EndPipelineMsgSerializer {

  implicit val serializerEnd: StepMsgSerializer[EndPipeline] = new StepMsgSerializer[EndPipeline] {

    private def toProto(msg: EndPipeline): StepMsgProto =
      StepMsgProto().withEnd(EndPipelineProtoMsg().withPipelineName(msg.pipelineName))

    private def fromProto(proto: StepMsgProto): Either[StepMsgSerializationError, EndPipeline] = {
      Right[StepMsgSerializationError, EndPipeline](EndPipeline(proto.getEnd.pipelineName))
    }

    override def toBytes(msg: EndPipeline): Array[Byte] = toProto(msg).toByteArray

    override def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, EndPipeline] =
      fromProto(StepMsgProto.parseFrom(bytes))
  }
}
