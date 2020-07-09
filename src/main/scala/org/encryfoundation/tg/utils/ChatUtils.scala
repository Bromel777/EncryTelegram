package org.encryfoundation.tg.utils

import org.drinkless.tdlib.TdApi

object ChatUtils {

  def getUnreadMsgsCount(chat: TdApi.Chat): Int = if (chat.unreadCount == null) 0 else chat.unreadCount
}
