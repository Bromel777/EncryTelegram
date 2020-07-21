package org.encryfoundation.tg.pipelines.messages.serializer

import StepProto.StepMsgProto
import cats.instances.either._
import org.encryfoundation.tg.pipelines.messages.StepMsg
import org.encryfoundation.tg.pipelines.messages.serializer.StepMsgSerializationError.CorruptedBytes
import org.encryfoundation.tg.pipelines.messages.serializer.groupVerification.{GroupVerificationStepMsgSerializer, VerifierSecondMsgSerializer}
import org.encryfoundation.tg.pipelines.messages.serializer.utilsMsg.UtilsMsgSerializer

import scala.util.{Failure, Success, Try}

trait StepMsgSerializer[M <: StepMsg] {
  def toProto(msg: M): StepMsgProto
  def parseProto(msgProto: StepMsgProto): Either[StepMsgSerializationError, M]
  def toBytes(msg: M): Array[Byte] = toProto(msg).toByteArray
  def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, M] = parseProto(StepMsgProto.parseFrom(bytes))
}

object StepMsgSerializer {
  def toBytes[M <: StepMsg](msg: M)(implicit serializer: StepMsgSerializer[M]): Array[Byte] =
    serializer.toBytes(msg)

  def parseMsgBytes[M <: StepMsg](bytes: Array[Byte])
                                 (implicit serializer: StepMsgSerializer[M]): Either[StepMsgSerializationError, M] =
    serializer.parseBytes(bytes)

  def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, StepMsg] = {
    Try(StepMsgProto.parseFrom(bytes)) match {
      case Success(msg) =>
        if (msg.stepMsg.isStart) StartPipelineMsgSerializer.serializerStart.parseBytes(bytes)
        else if (msg.stepMsg.isEnd) EndPipelineMsgSerializer.serializerEnd.parseBytes(bytes)
        else if (msg.stepMsg.isVerification) GroupVerificationStepMsgSerializer.parseBytes(bytes)
        else if (msg.stepMsg.isWelcome) UtilsMsgSerializer.parseBytes(bytes)
        else Left[StepMsgSerializationError, StepMsg](CorruptedBytes)
      case Failure(exception) =>
        println(exception.getMessage)
        Left[StepMsgSerializationError, StepMsg](CorruptedBytes)
    }
  }
}
