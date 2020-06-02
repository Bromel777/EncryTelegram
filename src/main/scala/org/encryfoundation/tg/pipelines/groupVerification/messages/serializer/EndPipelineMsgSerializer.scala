package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer

import EndPipelineProto.EndPipelineProtoMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.EndPipeline

object EndPipelineMsgSerializer {

  implicit val serializer: StepMsgSerializer[EndPipeline] = new StepMsgSerializer[EndPipeline] {

    private def toProto(msg: EndPipeline): EndPipelineProtoMsg =
      EndPipelineProtoMsg().withPipelineName(msg.pipelineName)

    private def fromProto(proto: EndPipelineProtoMsg): Either[StepMsgSerializationError, EndPipeline] = {
      Right[StepMsgSerializationError, EndPipeline](EndPipeline(proto.pipelineName))
    }

    override def toBytes(msg: EndPipeline): Array[Byte] = toProto(msg).toByteArray

    override def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, EndPipeline] =
      fromProto(EndPipelineProtoMsg.parseFrom(bytes))
  }
}
