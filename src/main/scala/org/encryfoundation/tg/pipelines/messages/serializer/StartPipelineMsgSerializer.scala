package org.encryfoundation.tg.pipelines.messages.serializer

import StartPipelineProto.StartPipelineProtoMsg
import StepProto.StepMsgProto
import org.encryfoundation.tg.pipelines.messages.StepMsg.StartPipeline

object StartPipelineMsgSerializer {

  implicit val serializerStart: StepMsgSerializer[StartPipeline] = new StepMsgSerializer[StartPipeline] {
    def toProto(msg: StartPipeline): StepMsgProto =
      StepMsgProto().withStart(StartPipelineProtoMsg().withPipelineName(msg.pipelineName))

    def parseProto(proto: StepMsgProto): Either[StepMsgSerializationError, StartPipeline] = {
      Right[StepMsgSerializationError, StartPipeline](StartPipeline(proto.getStart.pipelineName))
    }
  }
}
