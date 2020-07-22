package org.encryfoundation.tg.pipelines.messages.serializer.groupVerification

import GroupVerificationProto.GroupVerificationProtoMsg
import ProverThirdStepProto.ProverThirdStepProtoMsg
import StepProto.StepMsgProto
import com.google.protobuf.ByteString
import org.encryfoundation.tg.pipelines.messages.StepMsg.GroupVerificationStepMsg.ProverThirdStepMsg
import org.encryfoundation.tg.pipelines.messages.serializer.{StepMsgSerializationError, StepMsgSerializer}
import org.encryfoundation.tg.userState.PrivateGroupChat

object ProverThirdMsgSerializer {

  implicit val serializer: StepMsgSerializer[ProverThirdStepMsg] = new StepMsgSerializer[ProverThirdStepMsg] {

    def toProto(msg: ProverThirdStepMsg): StepMsgProto =
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
    def parseProto(protoMsg: StepMsgProto): Either[StepMsgSerializationError, ProverThirdStepMsg] = {
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
  }
}
