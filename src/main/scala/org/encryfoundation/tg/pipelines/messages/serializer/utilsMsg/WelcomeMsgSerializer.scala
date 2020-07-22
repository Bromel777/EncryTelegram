package org.encryfoundation.tg.pipelines.messages.serializer.utilsMsg

import StepProto.StepMsgProto
import WelcomeResponseProto.WelcomeResponseProtoMsg
import org.encryfoundation.tg.pipelines.messages.StepMsg.GroupVerificationStepMsg.{ProverFirstStepMsg, ProverThirdStepMsg}
import org.encryfoundation.tg.pipelines.messages.StepMsg.WelcomeMsg.WelcomeResponseMsg
import org.encryfoundation.tg.pipelines.messages.serializer.{StepMsgSerializationError, StepMsgSerializer}
import com.google.protobuf.ByteString

object WelcomeMsgSerializer {

  implicit val welcomeSerializer = new StepMsgSerializer[WelcomeResponseMsg] {

    def toProto(msg: WelcomeResponseMsg): StepMsgProto = StepMsgProto()
      .withWelcome(WelcomeResponseProtoMsg().withMsgHash(ByteString.copyFrom(msg.msgHash)))

    def parseProto(msg: StepMsgProto): Either[StepMsgSerializationError, WelcomeResponseMsg] = {
      val proto = msg.getWelcome
      Right(WelcomeResponseMsg(proto.msgHash.toByteArray))
    }
  }
}
