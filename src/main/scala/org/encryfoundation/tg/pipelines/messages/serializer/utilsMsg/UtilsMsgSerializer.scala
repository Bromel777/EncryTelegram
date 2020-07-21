package org.encryfoundation.tg.pipelines.messages.serializer.utilsMsg

import StepProto.StepMsgProto
import org.encryfoundation.tg.pipelines.messages.StepMsg.WelcomeMsg
import org.encryfoundation.tg.pipelines.messages.serializer.StepMsgSerializationError
import org.encryfoundation.tg.pipelines.messages.serializer.StepMsgSerializationError.CorruptedBytes

object UtilsMsgSerializer {

  def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, WelcomeMsg] = {
    val parse = StepMsgProto.parseFrom(bytes)
    if (parse.stepMsg.isWelcome) WelcomeMsgSerializer.welcomeSerializer.parseBytes(bytes)
    else Left[StepMsgSerializationError, WelcomeMsg](CorruptedBytes)
  }
}
