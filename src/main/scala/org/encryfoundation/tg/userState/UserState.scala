package org.encryfoundation.tg.userState

import org.drinkless.tdlib.{Client, Client123, TdApi}
import org.encryfoundation.tg.community.{InviteStatus, PrivateCommunityStatus}

case class UserState[F[_]](mainChatList: List[TdApi.Chat] = List.empty,
                           chatIds: Map[Long, TdApi.Chat] = Map.empty,
                           privateGroups: Map[Long, (TdApi.Chat, String, PrivateCommunityStatus)] = Map.empty,
                           pendingSecretChatsForInvite: Map[Long, (TdApi.Chat, String, Long)] = Map.empty,
                           inviteChats: Map[Long, InviteStatus] = Map.empty,
                           users: Map[Int, TdApi.User] = Map.empty,
                           basicGroups: Map[Int, TdApi.BasicGroup] = Map.empty,
                           superGroups: Map[Int, TdApi.Supergroup] = Map.empty,
                           secretChats: Map[Int, TdApi.SecretChat] = Map.empty,
                           isAuth: Boolean = false,
                           client: Client[F])
