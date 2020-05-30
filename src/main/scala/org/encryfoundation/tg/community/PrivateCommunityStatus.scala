package org.encryfoundation.tg.community

import org.encryfoundation.tg.community.PrivateCommunityStatus.UserCommunityStatus
import org.encryfoundation.tg.community.PrivateCommunityStatus.UserCommunityStatus.{Active, AwaitingInvite}

case class PrivateCommunityStatus(groupName: String,
                                  privateCommunity: PrivateCommunity,
                                  password: String,
                                  usersStatus: Map[String, UserCommunityStatus])

object PrivateCommunityStatus {
  sealed trait UserCommunityStatus
  object UserCommunityStatus {
    case object AwaitingInvite extends UserCommunityStatus
    case object AwaitingFirstPhase extends UserCommunityStatus
    case object AwaitingSecondPhaseFromUser extends UserCommunityStatus
    case object AwaitingThirdPhase extends UserCommunityStatus
    case object Active extends UserCommunityStatus
  }

  def getNewInfoForChat(myLogin: String, password: String, privateCommunity: PrivateCommunity): PrivateCommunityStatus =
    PrivateCommunityStatus(
      privateCommunity.name,
      privateCommunity,
      password,
      privateCommunity.users.map {
        case user if user.userTelegramLogin == myLogin => myLogin -> Active
        case user => user.userTelegramLogin -> AwaitingInvite
      }.toMap
    )
}


