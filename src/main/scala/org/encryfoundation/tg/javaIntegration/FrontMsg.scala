package org.encryfoundation.tg.javaIntegration

import org.drinkless.tdlib.TdApi
import org.javaFX.model.nodes.{VBoxChatCell, VBoxMessageCell}

trait FrontMsg {
  def code: Int
}

object FrontMsg {

  object Codes {
    def error = -1
    def loadVc = 0
    def loadPass = 1
    def loadChats = 2
    def newMsgInChat = 3
    def newChat = 4
    def updateLastMsg = 5
  }

  object LoadVCWindow extends FrontMsg {
    def code: Int = Codes.loadVc
  }
  object LoadPassWindow extends FrontMsg {
    def code: Int = Codes.loadPass
  }
  object LoadChatsWindow extends FrontMsg {
    def code: Int = Codes.loadChats
  }

  case class NewMsgInChat(msg: VBoxMessageCell) extends FrontMsg {
    def code: Int = Codes.newMsgInChat
  }

  case class NewChat(chat: TdApi.Chat) extends FrontMsg {
    def code: Int = Codes.newChat
  }

  case class UpdateLastMsg(chatId: Long, msgText: String, msgTime: Long) extends FrontMsg {
    def code: Int = Codes.updateLastMsg
  }

  object Error extends FrontMsg {
    def code: Int = Codes.error
  }
}
