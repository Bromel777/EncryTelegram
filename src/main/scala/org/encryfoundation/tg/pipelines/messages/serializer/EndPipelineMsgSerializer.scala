package org.encryfoundation.tg.pipelines.messages.serializer

import EndPipelineProto.EndPipelineProtoMsg
import GroupVerificationProto.GroupVerificationProtoMsg
import StepProto.StepMsgProto
import org.encryfoundation.tg.pipelines.messages.StepMsg.EndPipeline

object EndPipelineMsgSerializer {

  implicit val serializerEnd: StepMsgSerializer[EndPipeline] = new StepMsgSerializer[EndPipeline] {
    def toProto(msg: EndPipeline): StepMsgProto =
      StepMsgProto().withEnd(EndPipelineProtoMsg().withPipelineName(msg.pipelineName))

    def parseProto(proto: StepMsgProto): Either[StepMsgSerializationError, EndPipeline] = {
      Right[StepMsgSerializationError, EndPipeline](EndPipeline(proto.getEnd.pipelineName))
    }
  }
}
