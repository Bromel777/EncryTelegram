package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.groupVerification

import GroupVerificationProto.GroupVerificationProtoMsg
import StepProto.StepMsgProto
import VerifierSecondStepProto.VerifierSecondStepProtoMsg
import com.google.protobuf.ByteString
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.GroupVerificationStepMsg.VerifierSecondStepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.{StepMsgSerializationError, StepMsgSerializer}

object VerifierSecondMsgSerializer {

  implicit val serializer: StepMsgSerializer[VerifierSecondStepMsg] = new StepMsgSerializer[VerifierSecondStepMsg] {

    private def toProto(msg: VerifierSecondStepMsg): StepMsgProto =
      StepMsgProto()
          .withVerification(
            GroupVerificationProtoMsg()
              .withVerSec(VerifierSecondStepProtoMsg()
                .withSecondStep(ByteString.copyFrom(msg.secondStep.toBytes))
                .withVerifierPubKey(ByteString.copyFrom(msg.verifierPubKey1.toBytes)))
          )

    private def parseProto(protoMsg: StepMsgProto): Either[StepMsgSerializationError, VerifierSecondStepMsg] = {
      val proto = protoMsg.getVerification.getVerSec
      val pairing = PairingFactory.getPairing("src/main/resources/properties/a.properties")
      val secondStep = pairing.getG1.newElementFromBytes(proto.secondStep.toByteArray).getImmutable
      val verifierPublicKey1 = pairing.getGT.newElementFromBytes(proto.verifierPubKey.toByteArray).getImmutable
      Right[StepMsgSerializationError, VerifierSecondStepMsg](VerifierSecondStepMsg(verifierPublicKey1, secondStep))
    }

    override def toBytes(msg: VerifierSecondStepMsg): Array[Byte] = {
      println("here bytes")
      toProto(msg).toByteArray
    }

    override def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, VerifierSecondStepMsg] =
      parseProto(StepMsgProto.parseFrom(bytes))
  }
}
