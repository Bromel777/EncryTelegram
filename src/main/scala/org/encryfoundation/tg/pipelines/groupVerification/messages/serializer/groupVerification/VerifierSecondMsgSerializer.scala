package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.groupVerification

import VerifierSecondStepProto.VerifierSecondStepProtoMsg
import com.google.protobuf.ByteString
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.GroupVerificationStepMsg.VerifierSecondStepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.{StepMsgSerializationError, StepMsgSerializer}

object VerifierSecondMsgSerializer {

  implicit val serializer: StepMsgSerializer[VerifierSecondStepMsg] = new StepMsgSerializer[VerifierSecondStepMsg] {

    private def toProto(msg: VerifierSecondStepMsg): VerifierSecondStepProtoMsg =
      VerifierSecondStepProtoMsg()
        .withSecondStep(ByteString.copyFrom(msg.secondStep.toBytes))
        .withVerifierPubKey(ByteString.copyFrom(msg.verifierPubKey1.toBytes))

    private def parseProto(proto: VerifierSecondStepProtoMsg): Either[StepMsgSerializationError, VerifierSecondStepMsg] = {
      val pairing = PairingFactory.getPairing("src/main/resources/properties/a.properties")
      val secondStep = pairing.getG1.newElementFromBytes(proto.secondStep.toByteArray).getImmutable
      val verifierPublicKey1 = pairing.getGT.newElementFromBytes(proto.secondStep.toByteArray).getImmutable
      Right[StepMsgSerializationError, VerifierSecondStepMsg](VerifierSecondStepMsg(verifierPublicKey1, secondStep))
    }

    override def toBytes(msg: VerifierSecondStepMsg): Array[Byte] = toProto(msg).toByteArray

    override def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, VerifierSecondStepMsg] =
      parseProto(VerifierSecondStepProtoMsg.parseFrom(bytes))
  }
}
