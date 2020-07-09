package org.encryfoundation.tg.javaIntegration

import javafx.scene.control.TextArea
import org.javaFX.model.JDialog


sealed trait JavaInterMsg
object JavaInterMsg {

  case class SetActiveChat(id: Long) extends JavaInterMsg
  //will send msg to active chat
  case class SendToChat(msg: String) extends JavaInterMsg
  case class CreateCommunityJava(name: String, users: java.util.List[String]) extends JavaInterMsg
  case class DeleteCommunity(name: String) extends JavaInterMsg
  case class CreatePrivateGroupChat(comName: String) extends JavaInterMsg
  case class SetVCCode(vcCode: String) extends JavaInterMsg
  case class SetPhone(phone: String) extends JavaInterMsg
  case class SetPass(pass: String) extends JavaInterMsg
  case class Logout() extends JavaInterMsg
}
