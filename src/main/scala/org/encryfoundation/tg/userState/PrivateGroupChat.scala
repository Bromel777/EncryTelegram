package org.encryfoundation.tg.userState

import PrivateGroupChatProto.PrivateGroupChatProtoMessage

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
    .withGroupName(pgc.communityName)
    .withPassword(pgc.password)

  def fromProto(pgcProto: PrivateGroupChatProtoMessage): PrivateGroupChat =
    PrivateGroupChat(pgcProto.chatId, pgcProto.communityName, pgcProto.groupName, pgcProto.password)

  def toBytes(pgc: PrivateGroupChat): Array[Byte] = toProto(pgc).toByteArray

  def parseBytes(bytes: Array[Byte]): Try[PrivateGroupChat] =
    Try(fromProto(PrivateGroupChatProtoMessage.parseFrom(bytes)))
}
