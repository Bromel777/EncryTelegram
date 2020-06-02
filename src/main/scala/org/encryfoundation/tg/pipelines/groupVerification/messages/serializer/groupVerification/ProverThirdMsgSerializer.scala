package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.groupVerification

import ProverThirdStepProto.ProverThirdStepProtoMsg
import com.google.protobuf.ByteString
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.ProverThirdStepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.{StepMsgSerializationError, StepMsgSerializer}

object ProverThirdMsgSerializer {

  implicit val serializer: StepMsgSerializer[ProverThirdStepMsg] = new StepMsgSerializer[ProverThirdStepMsg] {

    private def toProto(msg: ProverThirdStepMsg): ProverThirdStepProtoMsg =
      ProverThirdStepProtoMsg().withThirdStep(ByteString.copyFrom(msg.thirdStep))

    //todo: check for errs
    private def parseProto(proto: ProverThirdStepProtoMsg): Either[StepMsgSerializationError, ProverThirdStepMsg] = {
      Right[StepMsgSerializationError, ProverThirdStepMsg](ProverThirdStepMsg(proto.thirdStep.toByteArray))
    }

    override def toBytes(msg: ProverThirdStepMsg): Array[Byte] = toProto(msg).toByteArray

    override def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, ProverThirdStepMsg] =
      parseProto(ProverThirdStepProtoMsg.parseFrom(bytes))
  }
}
