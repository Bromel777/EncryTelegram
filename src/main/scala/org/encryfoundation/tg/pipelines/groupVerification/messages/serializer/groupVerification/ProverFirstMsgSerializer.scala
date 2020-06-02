package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.groupVerification

import ProverFistStepProto.ProverFirstStepProtoMsg
import com.google.protobuf.ByteString
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.ProverFirstStepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.{StepMsgSerializationError, StepMsgSerializer}

object ProverFirstMsgSerializer {

  implicit val serializer: StepMsgSerializer[ProverFirstStepMsg] = new StepMsgSerializer[ProverFirstStepMsg] {
    private def toProto(msg: ProverFirstStepMsg): ProverFirstStepProtoMsg =
      ProverFirstStepProtoMsg()
        .withFirstStep(ByteString.copyFrom(msg.firstStep.toBytes))
        .withG1Gen(ByteString.copyFrom(msg.g1Gen.toBytes))
        .withG2Gen(ByteString.copyFrom(msg.g2Gen.toBytes))
        .withGTilda(ByteString.copyFrom(msg.gTilda.toBytes))
        .withProverPublicKey1(ByteString.copyFrom(msg.proverPublicKey1.toBytes))
        .withProverPublicKey2(ByteString.copyFrom(msg.proverPublicKey2.toBytes))
        .withZRGen(ByteString.copyFrom(msg.zRGen.toBytes))

    //todo: add exception check
    private def fromProto(protoMsg: ProverFirstStepProtoMsg): Either[StepMsgSerializationError, ProverFirstStepMsg] = {
      val pairing = PairingFactory.getPairing("src/main/resources/properties/a.properties")
      val firstStepBytes = pairing.getG1.newElementFromBytes(protoMsg.firstStep.toByteArray).getImmutable
      val g1Gen = pairing.getG1.newElementFromBytes(protoMsg.g1Gen.toByteArray).getImmutable
      val g2Gen = pairing.getG2.newElementFromBytes(protoMsg.g2Gen.toByteArray).getImmutable
      val gTilda = pairing.getGT.newElementFromBytes(protoMsg.gTilda.toByteArray).getImmutable
      val proverPublicKey1 = pairing.getGT.newElementFromBytes(protoMsg.proverPublicKey1.toByteArray).getImmutable
      val proverPublicKey2 = pairing.getGT.newElementFromBytes(protoMsg.proverPublicKey2.toByteArray).getImmutable
      val zRGen = pairing.getZr.newElementFromBytes(protoMsg.zRGen.toByteArray).getImmutable
      Right[StepMsgSerializationError, ProverFirstStepMsg](
        ProverFirstStepMsg(
          firstStepBytes,
          gTilda,
          proverPublicKey1,
          proverPublicKey2,
          g1Gen,
          g2Gen,
          zRGen
        )
      )
    }

    def toBytes(msg: ProverFirstStepMsg): Array[Byte] = toProto(msg).toByteArray
    def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, ProverFirstStepMsg] =
      fromProto(ProverFirstStepProtoMsg.parseFrom(bytes))
  }
}
