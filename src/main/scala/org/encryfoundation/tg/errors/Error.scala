package org.encryfoundation.tg.errors

sealed trait TdError extends Throwable
object TdError {

  sealed trait PrivateChatCreationError extends TdError
  object PrivateChatCreationError {

    case class RecipientNotFound(recipientIdentifier: String) extends PrivateChatCreationError
  }

  sealed trait PrivateGroupChatCreationError extends TdError
  object PrivateGroupChatCreationError {

    case class RecipientNotFound(recipientIdentifier: String) extends PrivateGroupChatCreationError
    case class PrivateCommunityNotFound(communityIdentifier: String) extends PrivateGroupChatCreationError
    case object ErrorAtFirstStep extends PrivateGroupChatCreationError
    case object ErrorAtSecondStep extends PrivateGroupChatCreationError
    case object ErrorAtThirdStep extends PrivateGroupChatCreationError
  }
}
