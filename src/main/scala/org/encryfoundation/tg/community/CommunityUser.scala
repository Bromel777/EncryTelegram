package org.encryfoundation.tg.community

import org.encryfoundation.sectionSeven.User

import scala.util.Try

case class CommunityUser(userTelegramLogin: String,
                         userData: User)

object CommunityUser {

  def toBytes(cu: CommunityUser): Array[Byte] = ???
  def parseBytes(bytes: Array[Byte]): Try[CommunityUser] = ???
}