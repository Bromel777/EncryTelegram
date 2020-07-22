package org.encryfoundation.tg.pipelines.messages.serializer.groupVerification

import GroupVerificationProto.GroupVerificationProtoMsg
import ProverFistStepProto.ProverFirstStepProtoMsg
import StepProto.StepMsgProto
import com.google.protobuf.ByteString
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.tg.pipelines.messages.StepMsg.GroupVerificationStepMsg.ProverFirstStepMsg
import org.encryfoundation.tg.pipelines.messages.serializer.{StepMsgSerializationError, StepMsgSerializer}

object ProverFirstMsgSerializer {

  implicit val serializerProverFirst: StepMsgSerializer[ProverFirstStepMsg] = new StepMsgSerializer[ProverFirstStepMsg] {

    def toProto(msg: ProverFirstStepMsg): StepMsgProto =
      StepMsgProto()
          .withVerification(
            GroupVerificationProtoMsg()
              .withProFir(ProverFirstStepProtoMsg()
                .withFirstStep(ByteString.copyFrom(msg.firstStep.toBytes))
                .withG1Gen(ByteString.copyFrom(msg.g1Gen.toBytes))
                .withG2Gen(ByteString.copyFrom(msg.g2Gen.toBytes))
                .withGTilda(ByteString.copyFrom(msg.gTilda.toBytes))
                .withProverPublicKey1(ByteString.copyFrom(msg.proverPublicKey1.toBytes))
                .withProverPublicKey2(ByteString.copyFrom(msg.proverPublicKey2.toBytes))
                .withZRGen(ByteString.copyFrom(msg.zRGen.toBytes)))
          )

    //todo: add exception check
    def parseProto(msg: StepMsgProto): Either[StepMsgSerializationError, ProverFirstStepMsg] = {
      val protoMsg = msg.getVerification.getProFir
      val pairing = PairingFactory.getPairing("properties/a.properties")
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
  }
}
