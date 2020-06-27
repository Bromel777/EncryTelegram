package org.encryfoundation.tg.userState

import PrivateGroupChatProto.PrivateGroupChatProtoMessage
import PrivateGroupChatsProto.PrivateGroupChatsProtoMessage

import scala.util.Try

final case class PrivateGroupChat(chatId: Long,
                                  communityName: String,
                                  groupName: String,
                                  password: String)

object PrivateGroupChat {
  def toProto(pgc: PrivateGroupChat): PrivateGroupChatProtoMessage =
    PrivateGroupChatProtoMessage()
    .withChatId(pgc.chatId)
    .withCommunityName(pgc.communityName)
    .withGroupName(pgc.groupName)
    .withPassword(pgc.password)

  def fromProto(pgcProto: PrivateGroupChatProtoMessage): PrivateGroupChat =
    PrivateGroupChat(pgcProto.chatId, pgcProto.communityName, pgcProto.groupName, pgcProto.password)

  def toBytes(pgc: PrivateGroupChat): Array[Byte] = toProto(pgc).toByteArray

  def parseBytes(bytes: Array[Byte]): Try[PrivateGroupChat] =
    Try(fromProto(PrivateGroupChatProtoMessage.parseFrom(bytes)))
}

object PrivateGroupChats {
  def toProto(pgc: List[PrivateGroupChat]): PrivateGroupChatsProtoMessage =
    PrivateGroupChatsProtoMessage()
      .withPrivateGroupChats(pgc.map(PrivateGroupChat.toProto))

  def fromProto(pgcProto: PrivateGroupChatsProtoMessage): List[PrivateGroupChat] =
    pgcProto.privateGroupChats.map(PrivateGroupChat.fromProto).toList

  def toBytes(pgc: List[PrivateGroupChat]): Array[Byte] = toProto(pgc).toByteArray

  def parseBytes(bytes: Array[Byte]): Try[List[PrivateGroupChat]] =
    Try(fromProto(PrivateGroupChatsProtoMessage.parseFrom(bytes)))
}
