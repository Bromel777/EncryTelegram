package org.encryfoundation.tg.community

import PrivateCommunityProto.PrivateCommunityProtoMessage
import cats.effect.Sync
import com.google.protobuf.ByteString
import it.unisa.dia.gas.jpbc.{Element, Pairing}
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.sectionSeven.User

import scala.util.Try

case class PrivateCommunity(name: String,
                            users: List[CommunityUser],
                            G1Gen: Element,
                            G2Gen: Element,
                            ZrGen: Element,
                            gTilda: Element)

object PrivateCommunity {

  def toProto(pc: PrivateCommunity): PrivateCommunityProtoMessage = {
    PrivateCommunityProtoMessage()
      .withName(pc.name)
      .withUsers(pc.users.map(CommunityUser.toProto(_, pc.G1Gen, pc.G2Gen, pc.ZrGen)))
      .withG1Gen(ByteString.copyFrom(pc.G1Gen.toBytes))
      .withG2Gen(ByteString.copyFrom(pc.G2Gen.toBytes))
      .withZrGen(ByteString.copyFrom(pc.ZrGen.toBytes))
      .withGTilda(ByteString.copyFrom(pc.gTilda.toBytes))
  }

  def fromProto(pc: PrivateCommunityProtoMessage): PrivateCommunity = {
    val name = pc.name
    val users = pc.users.toList.map(CommunityUser.fromProto)
    val pairing: Pairing = PairingFactory.getPairing("properties/a.properties")
    val g1Gen = pairing.getG1.newElementFromBytes(pc.g1Gen.toByteArray)
    val g2Gen = pairing.getG1.newElementFromBytes(pc.g2Gen.toByteArray)
    val zRGen = pairing.getZr.newElementFromBytes(pc.zrGen.toByteArray)
    val gTilda = pairing.getGT.newElementFromBytes(pc.gTilda.toByteArray)
    PrivateCommunity(name, users, g1Gen, g2Gen, zRGen, gTilda)
  }

  def toBytes(pc: PrivateCommunity): Array[Byte] = toProto(pc).toByteArray

  def parseBytes(bytes: Array[Byte]): Try[PrivateCommunity] =
    Try(fromProto(PrivateCommunityProtoMessage.parseFrom(bytes)))

  implicit val privComOps: CommunityOps[PrivateCommunity] = new CommunityOps[PrivateCommunity]
  {
    override def addUser(com: PrivateCommunity, newUser: CommunityUser): PrivateCommunity =
      com.copy(users = (com.users :+ newUser))

    override def deleteUser(com: PrivateCommunity, userLogin: String): PrivateCommunity =
      com.copy(users = com.users.filter(_.userTelegramLogin == userLogin))
  }
}
