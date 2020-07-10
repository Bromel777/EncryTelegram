package org.drinkless.tdlib

import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.handlers.EmptyHandler
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base64
import cats.implicits._
import org.encryfoundation.tg.services.ClientService

object ClientUtils {

  def sendMessage[F[_]: Concurrent: Logger](chatId: Long, msg: String, clientService: ClientService[F]): F[Unit] = {
    val row: Array[TdApi.InlineKeyboardButton] = Array(
      new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()),
      new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()),
      new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())
    )
    val replyMarkup: TdApi.ReplyMarkup = new TdApi.ReplyMarkupInlineKeyboard(Array(row, row, row))
    val content: TdApi.InputMessageContent = new TdApi.InputMessageText(new TdApi.FormattedText(msg, null), false, true)
    clientService.sendRequest(new TdApi.SendMessage(chatId, 0, null, replyMarkup, content), EmptyHandler[F]())
  }

  def sendMsg[F[_]: Concurrent: Logger](chat: TdApi.Chat,
                                        msg: String,
                                        stateRef: Ref[F, UserState[F]],
                                        clientService: ClientService[F]): F[Unit] = {

    stateRef.get.flatMap(state =>
      state.privateGroups.find(_.chatId == chat.id).map { privGroup =>
        val aes = AESEncryption(privGroup.password.getBytes())
        sendMessage(chat.id, Base64.encode(aes.encrypt(msg.getBytes)), clientService)
      }.getOrElse(sendMessage(chat.id, msg, clientService))
    )
  }
}
