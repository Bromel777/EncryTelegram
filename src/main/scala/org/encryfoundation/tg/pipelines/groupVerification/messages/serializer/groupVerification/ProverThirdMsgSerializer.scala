package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.groupVerification

import GroupVerificationProto.GroupVerificationProtoMsg
import ProverThirdStepProto.ProverThirdStepProtoMsg
import StepProto.StepMsgProto
import com.google.protobuf.ByteString
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.GroupVerificationStepMsg.ProverThirdStepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.{StepMsgSerializationError, StepMsgSerializer}
import org.encryfoundation.tg.userState.PrivateGroupChat

object ProverThirdMsgSerializer {

  implicit val serializer: StepMsgSerializer[ProverThirdStepMsg] = new StepMsgSerializer[ProverThirdStepMsg] {

    private def toProto(msg: ProverThirdStepMsg): StepMsgProto =
      StepMsgProto()
          .withVerification(
            GroupVerificationProtoMsg()
              .withProThi(
                ProverThirdStepProtoMsg()
                  .withThirdStep(ByteString.copyFrom(msg.thirdStep))
                  .withChatId(msg.chatId)
                  .withGroupName(msg.name)
                  .withPrivateGroup(PrivateGroupChat.toProto(msg.privateGroupChat))
              )
          )

    //todo: check for errs
    private def parseProto(protoMsg: StepMsgProto): Either[StepMsgSerializationError, ProverThirdStepMsg] = {
      val proto = protoMsg.getVerification.getProThi
      Right[StepMsgSerializationError, ProverThirdStepMsg](
        ProverThirdStepMsg(
          proto.thirdStep.toByteArray,
          proto.chatId,
          proto.groupName,
          PrivateGroupChat.fromProto(proto.privateGroup.get)
        )
      )
    }

    override def toBytes(msg: ProverThirdStepMsg): Array[Byte] = toProto(msg).toByteArray

    override def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, ProverThirdStepMsg] =
      parseProto(StepMsgProto.parseFrom(bytes))
  }
}
