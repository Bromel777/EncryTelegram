package org.encryfoundation.tg.userState

import org.drinkless.tdlib.{Client, Client123, TdApi}

case class UserState[F[_]](mainChatList: List[TdApi.Chat],
                           chatIds: Map[Long, TdApi.Chat],
                           isAuth: Boolean,
                           client: Client[F])
