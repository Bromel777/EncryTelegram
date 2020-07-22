package org.encryfoundation.tg.pipelines.messages.serializer.groupVerification

import GroupVerificationProto.GroupVerificationProtoMsg
import StepProto.StepMsgProto
import org.encryfoundation.tg.pipelines.messages.StepMsg.GroupVerificationStepMsg
import org.encryfoundation.tg.pipelines.messages.serializer.{StepMsgSerializationError, StepMsgSerializer}
import org.encryfoundation.tg.pipelines.messages.serializer.StepMsgSerializationError.CorruptedBytes

object GroupVerificationStepMsgSerializer {

  def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, GroupVerificationStepMsg] = {
    val parse = StepMsgProto.parseFrom(bytes)
    if (parse.getVerification.groupVerificationMsg.isProFir)
      ProverFirstMsgSerializer.serializerProverFirst.parseBytes(bytes)
    else if (parse.getVerification.groupVerificationMsg.isProThi)
      ProverThirdMsgSerializer.serializer.parseBytes(bytes)
    else if (parse.getVerification.groupVerificationMsg.isVerSec)
      VerifierSecondMsgSerializer.serializer.parseBytes(bytes)
    else Left[StepMsgSerializationError, GroupVerificationStepMsg](CorruptedBytes)
  }
}
