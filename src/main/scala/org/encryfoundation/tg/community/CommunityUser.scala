package org.encryfoundation.tg.community

import CommunityUserProto.CommunityUserProtoMessage
import UserDataProto.UserDataProtoMessage
import com.google.protobuf.ByteString
import it.unisa.dia.gas.jpbc.{Element, Pairing}
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.sectionSeven.User

import scala.util.Try

case class CommunityUser(userTelegramLogin: String,
                         userData: User)

object CommunityUser {

  def toProto(cu: CommunityUser,
              g1Gen: Element,
              g2Gen: Element,
              ZrGen: Element): CommunityUserProtoMessage = {
    val userMsg = UserDataProtoMessage()
      .withUserKsi(ByteString.copyFrom(cu.userData.userKsi.toBytes))
      .withUserT(ByteString.copyFrom(cu.userData.userT.toBytes))
      .withPublicKey1(ByteString.copyFrom(cu.userData.publicKey1.toBytes))
      .withPublicKey2(ByteString.copyFrom(cu.userData.publicKey2.toBytes))
      .withCommonPublicKey(ByteString.copyFrom(cu.userData.commonPublicKey.toBytes))
      .withG1Gen(ByteString.copyFrom(g1Gen.toBytes))
      .withG2Gen(ByteString.copyFrom(g2Gen.toBytes))
      .withZrGen(ByteString.copyFrom(ZrGen.toBytes))
    CommunityUserProtoMessage()
      .withUserTelegramLogin(cu.userTelegramLogin)
      .withData(userMsg)
  }

  def fromProto(cu: CommunityUserProtoMessage): CommunityUser = {
    val login = cu.userTelegramLogin
    val pairing: Pairing = PairingFactory.getPairing("src/main/resources/properties/a.properties")
    val g1Gen = pairing.getG1.newElementFromBytes(cu.g1Gen.toByteArray)
    val g2Gen = pairing.getG2.newElementFromBytes(cu.g2Gen.toByteArray)
    val zRGen = pairing.getZr.newElementFromBytes(cu.zrGen.toByteArray)
    val userKsi = pairing.getZr.newElementFromBytes(cu.data.get.userKsi.toByteArray)
    val userT = pairing.getG1.newElementFromBytes(cu.data.get.userT.toByteArray)
    val userPublicKey1 = pairing.getGT.newElementFromBytes(cu.data.get.publicKey1.toByteArray)
    val userPublicKey2 = pairing.getGT.newElementFromBytes(cu.data.get.publicKey2.toByteArray)
    val commonPublicKey = pairing.getGT.newElementFromBytes(cu.data.get.commonPublicKey.toByteArray)
    CommunityUser(
      login,
      User(userKsi, userT, userPublicKey1, userPublicKey2, commonPublicKey)
    )
  }

  def toBytes(cu: CommunityUser,
              g1Gen: Element,
              g2Gen: Element,
              ZrGen: Element): Array[Byte] = toProto(cu, g1Gen, g2Gen, ZrGen).toByteArray

  def parseBytes(bytes: Array[Byte]): Try[CommunityUser] =
    Try(fromProto(CommunityUserProtoMessage.parseFrom(bytes)))
}