package org.drinkless.tdlib

import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.handlers.EmptyHandler
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base64
import cats.implicits._

object ClientUtils {

  def sendMessage[F[_]: Concurrent: Logger](chatId: Long, msg: String, client: Client[F]): F[Unit] = {
    val row: Array[TdApi.InlineKeyboardButton] = Array(
      new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()),
      new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()),
      new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())
    )
    val replyMarkup: TdApi.ReplyMarkup = new TdApi.ReplyMarkupInlineKeyboard(Array(row, row, row))
    val content: TdApi.InputMessageContent = new TdApi.InputMessageText(new TdApi.FormattedText(msg, null), false, true)
    client.send(new TdApi.SendMessage(chatId, 0, null, replyMarkup, content), EmptyHandler[F]())
  }

  def sendMsg[F[_]: Concurrent: Logger](chat: TdApi.Chat, msg: String, stateRef: Ref[F, UserState[F]]): F[Unit] = {

    stateRef.get.flatMap(state =>
      state.privateGroups.find(_._2._1.id == chat.id).map { case (_, (_, pass)) =>
        val aes = AESEncryption(pass.getBytes())
        sendMessage(chat.id, Base64.encode(aes.encrypt(msg.getBytes)), state.client)
      }.getOrElse(sendMessage(chat.id, msg, state.client))
    )
  }
}
