package org.encryfoundation.tg.utils

import java.text.SimpleDateFormat

import org.drinkless.tdlib.TdApi
import org.drinkless.tdlib.TdApi.{MessagePhoto, MessageText, MessageVideo}
import org.javaFX.model.JMessage
import org.javaFX.model.nodes.VBoxDialogTextMessageCell

object MessagesUtils {

  def processMessage(msg: TdApi.Message): String =
    if (msg == null) "No message"
    else msg.content match {
      case text: MessageText => text.text.text
      case _: MessagePhoto => "Unsupported msg type"
      case _: MessageVideo => "Unsupported msg type"
      case _ => "Unknown msg type"
    }

  def getLastMessageTime(msg: TdApi.Message): String =
    if (msg == null) "No message"
    else {
      val df = new SimpleDateFormat("HH:mm")
      df.format(msg.date * 1000L)
    }
}
