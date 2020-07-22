package org.encryfoundation.tg.pipelines.messages.serializer.groupVerification

import GroupVerificationProto.GroupVerificationProtoMsg
import StepProto.StepMsgProto
import VerifierSecondStepProto.VerifierSecondStepProtoMsg
import com.google.protobuf.ByteString
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.tg.pipelines.messages.StepMsg.GroupVerificationStepMsg.VerifierSecondStepMsg
import org.encryfoundation.tg.pipelines.messages.serializer.{StepMsgSerializationError, StepMsgSerializer}

object VerifierSecondMsgSerializer {

  implicit val serializer: StepMsgSerializer[VerifierSecondStepMsg] = new StepMsgSerializer[VerifierSecondStepMsg] {

    def toProto(msg: VerifierSecondStepMsg): StepMsgProto =
      StepMsgProto()
          .withVerification(
            GroupVerificationProtoMsg()
              .withVerSec(VerifierSecondStepProtoMsg()
                .withSecondStep(ByteString.copyFrom(msg.secondStep.toBytes))
                .withVerifierPubKey(ByteString.copyFrom(msg.verifierPubKey1.toBytes)))
          )

    def parseProto(protoMsg: StepMsgProto): Either[StepMsgSerializationError, VerifierSecondStepMsg] = {
      val proto = protoMsg.getVerification.getVerSec
      val pairing = PairingFactory.getPairing("properties/a.properties")
      val secondStep = pairing.getG1.newElementFromBytes(proto.secondStep.toByteArray).getImmutable
      val verifierPublicKey1 = pairing.getGT.newElementFromBytes(proto.verifierPubKey.toByteArray).getImmutable
      Right[StepMsgSerializationError, VerifierSecondStepMsg](VerifierSecondStepMsg(verifierPublicKey1, secondStep))
    }
  }
}
