package org.encryfoundation.tg.pipelines.messages.serializer

trait StepMsgSerializationError
object StepMsgSerializationError {
  case object CorruptedBytes extends StepMsgSerializationError
}
