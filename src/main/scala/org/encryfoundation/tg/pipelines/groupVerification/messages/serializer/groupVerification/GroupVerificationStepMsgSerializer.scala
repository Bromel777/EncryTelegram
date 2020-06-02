package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.groupVerification

import GroupVerificationProto.GroupVerificationProtoMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.GroupVerificationStepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializationError
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializationError.CorruptedBytes

object GroupVerificationStepMsgSerializer {

  def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, GroupVerificationStepMsg] = {
    val msg = GroupVerificationProtoMsg.parseFrom(bytes)
    if (msg.groupVerificationMsg.isProFir) ProverFirstMsgSerializer.serializer.parseBytes(bytes)
    else if (msg.groupVerificationMsg.isProThi) ProverThirdMsgSerializer.serializer.parseBytes(bytes)
    else if (msg.groupVerificationMsg.isVerSec) VerifierSecondMsgSerializer.serializer.parseBytes(bytes)
    else Left[StepMsgSerializationError, GroupVerificationStepMsg](CorruptedBytes)
  }
}
