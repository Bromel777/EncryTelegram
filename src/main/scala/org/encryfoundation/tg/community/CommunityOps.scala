package org.encryfoundation.tg.community

import simulacrum._

@typeclass trait CommunityOps[Com <: PrivateCommunity] {
  @op("|+|") def addUser(com: Com, newUser: CommunityUser): Com
  def deleteUser(com: Com, userLogin: String): Com
}
