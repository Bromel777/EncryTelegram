package org.encryfoundation.tg.javaIntegration

import javafx.scene.control.TextArea
import org.javaFX.model.JDialog

sealed trait BackMsg
object BackMsg {
  case class SetActiveChat(id: Long) extends BackMsg
  //will send msg to active chat
  case class SendToChat(msg: String) extends BackMsg
  case class CreateCommunityJava(name: String, users: java.util.List[String]) extends BackMsg
  case class DeleteCommunity(name: String) extends BackMsg
  case class CreatePrivateGroupChat(comName: String) extends BackMsg
  case class SetVCCode(vcCode: String) extends BackMsg
  case class SetPhone(phone: String) extends BackMsg
  case class SetPass(pass: String) extends BackMsg
  case class Logout() extends BackMsg
}
