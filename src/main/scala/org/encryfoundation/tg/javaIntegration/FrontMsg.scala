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
    def loadPhone = 3
    def newMsgInChat = 4
    def newMsgsInChat = 5
    def newChat = 6
    def updateLastMsg = 7
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
  object LoadPhoneWindow extends FrontMsg {
    def code: Int = Codes.loadPhone
  }

  case class NewMsgInChat(msg: VBoxMessageCell) extends FrontMsg {
    def code: Int = Codes.newMsgInChat
  }

  case class NewMsgsInChat(msgs: java.util.List[VBoxMessageCell]) extends FrontMsg {
    override def code: Int = Codes.newMsgsInChat
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
