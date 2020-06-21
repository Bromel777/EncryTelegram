package org.encryfoundation.tg.services

import org.drinkless.tdlib.TdApi

trait ChatsDbService[F[_]] {
  def persistChat(chat: TdApi.Chat): F[Unit]
  def persistPrivateGroupChat(chat: TdApi.Chat, communityName: String, password: String): F[Unit]
  def recoverChats: F[Unit]
}

object ChatsDbService {

}
