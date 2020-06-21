package org.encryfoundation.tg.userState

import ConferencesNameProto.ConferencesNameProtoMessage

import scala.util.Try

case class ConferencesNames(conferences: List[String])

object ConferencesNames {
  def toProto(confs: ConferencesNames): ConferencesNameProtoMessage =
    ConferencesNameProtoMessage()
    .withConfs(confs.conferences)

  def fromProto(proto: ConferencesNameProtoMessage): ConferencesNames =
    ConferencesNames(proto.confs.toList)

  def toBytes(confs: ConferencesNames): Array[Byte] = toProto(confs).toByteArray

  def parseBytes(bytes: Array[Byte]): Try[ConferencesNames] =
    Try(fromProto(ConferencesNameProtoMessage.parseFrom(bytes)))
}
