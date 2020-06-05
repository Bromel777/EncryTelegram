package org.encryfoundation.tg.pipelines.groupVerification.messages.serializer

trait StepMsgSerializationError
object StepMsgSerializationError {
  case object CorruptedBytes extends StepMsgSerializationError
}
