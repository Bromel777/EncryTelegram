package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer

import StepProto.StepMsgProto
import cats.instances.either._
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializationError.CorruptedBytes
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.groupVerification.{GroupVerificationStepMsgSerializer, VerifierSecondMsgSerializer}

import scala.util.{Failure, Success, Try}

trait StepMsgSerializer[M <: StepMsg] {
  def toBytes(msg: M): Array[Byte]
  def parseBytes(bytes: Array[Byte]): Either[StepMsgSerializationError, M]
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
        else Left[StepMsgSerializationError, StepMsg](CorruptedBytes)
      case Failure(exception) =>
        println(exception.getMessage)
        Left[StepMsgSerializationError, StepMsg](CorruptedBytes)
    }
  }
}
