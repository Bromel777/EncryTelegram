package org.encryfoundation.tg.community

import CommunityUserProto.CommunityUserProtoMessage
import UserDataProto.UserDataProtoMessage
import com.google.protobuf.ByteString
import org.encryfoundation.sectionSeven.User

import scala.util.Try

case class CommunityUser(userTelegramLogin: String,
                         userData: User)

object CommunityUser {

  def toProto(cu: CommunityUser): CommunityUserProtoMessage = {
    val userMsg = UserDataProtoMessage()
      .withUserKsi(ByteString.copyFrom(cu.userData.userKsi.toBytes))
      .withUserT(ByteString.copyFrom(cu.userData.userT.toBytes))
      .withPublicKey1(ByteString.copyFrom(cu.userData.publicKey1.toBytes))
      .withPublicKey2(ByteString.copyFrom(cu.userData.publicKey2.toBytes))
      .withCommonPublicKey(ByteString.copyFrom(cu.userData.commonPublicKey.toBytes))
    CommunityUserProtoMessage()
      .withUserTelegramLogin(cu.userTelegramLogin)
      .withData(userMsg)
  }

  def fromProto(cu: CommunityUserProtoMessage): CommunityUserProtoMessage = ???

  def toBytes(cu: CommunityUser): Array[Byte] = toProto(cu).toByteArray
  def parseBytes(bytes: Array[Byte]): Try[CommunityUser] = ???
}