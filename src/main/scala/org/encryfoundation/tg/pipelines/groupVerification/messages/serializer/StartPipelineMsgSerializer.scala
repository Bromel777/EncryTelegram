package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer

import StartPipelineProto.StartPipelineProtoMsg
import StepProto.StepMsgProto
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.StartPipeline

object StartPipelineMsgSerializer {
  implicit val serializerStart: StepMsgSerializer[StartPipeline] = new StepMsgSerializer[StartPipeline] {

    private def toProto(msg: StartPipeline): StepMsgProto =
      StepMsgProto().withStart(StartPipelineProtoMsg().withPipelineName(msg.pipelineName))

    private def fromProto(proto: StepMsgProto): Either[StepMsgSerializationError, StartPipeline] = {
      Right[StepMsgSerializationError, StartPipeline](StartPipeline(proto.getStart.pipelineName))
    }

    override def toBytes(msg: StartPipeline): Array[Byte] = toProto(msg).toByteArray

    override def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, StartPipeline] =
      fromProto(StepMsgProto.parseFrom(bytes))
  }
}
