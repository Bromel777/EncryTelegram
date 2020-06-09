package org.encryfoundation.tg.javaIntegration

import javafx.scene.control.TextArea
import org.javaFX.model.JDialog


sealed trait JavaInterMsg
object JavaInterMsg {

  case class SetActiveChat(id: Long) extends JavaInterMsg
  //will send msg to active chat
  case class SendToChat(msg: String) extends JavaInterMsg
  case class CreateCommunityJava(name: String, users: java.util.List[String]) extends JavaInterMsg
  case class CreatePrivateGroupChat(comName: String,
                                    chatName: String,
                                    pass: String,
                                    users: List[String]) extends JavaInterMsg
}
