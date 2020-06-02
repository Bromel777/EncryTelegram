package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer

import StartPipelineProto.StartPipelineProtoMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.StartPipeline

object StartPipelineMsgSerializer {
  implicit val serializer: StepMsgSerializer[StartPipeline] = new StepMsgSerializer[StartPipeline] {

    private def toProto(msg: StartPipeline): StartPipelineProtoMsg =
      StartPipelineProtoMsg().withPipelineName(msg.pipelineName)

    private def fromProto(proto: StartPipelineProtoMsg): Either[StepMsgSerializationError, StartPipeline] = {
      Right[StepMsgSerializationError, StartPipeline](StartPipeline(proto.pipelineName))
    }

    override def toBytes(msg: StartPipeline): Array[Byte] = toProto(msg).toByteArray

    override def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, StartPipeline] =
      fromProto(StartPipelineProtoMsg.parseFrom(bytes))
  }
}
