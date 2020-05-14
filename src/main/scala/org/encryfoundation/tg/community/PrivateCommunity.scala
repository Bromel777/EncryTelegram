package org.encryfoundation.tg.community

import cats.effect.Sync
import it.unisa.dia.gas.jpbc.Element
import org.encryfoundation.sectionSeven.User

case class PrivateCommunity(name: String,
                            users: List[CommunityUser])

object PrivateCommunity {

  implicit val privComOps: CommunityOps[PrivateCommunity] = new CommunityOps[PrivateCommunity]
  {
    override def addUser(com: PrivateCommunity, newUser: CommunityUser): PrivateCommunity =
      com.copy(users = (com.users :+ newUser))

    override def deleteUser(com: PrivateCommunity, userLogin: String): PrivateCommunity =
      com.copy(users = com.users.filter(_.userTelegramLogin == userLogin))
  }
}
