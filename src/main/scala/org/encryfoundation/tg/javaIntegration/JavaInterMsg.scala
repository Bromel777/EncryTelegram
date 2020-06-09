package org.encryfoundation.tg.javaIntegration

import javafx.scene.control.TextArea
import org.javaFX.model.JDialog

sealed trait JavaInterMsg
object JavaInterMsg {

  case class GetChatMsgs(id: Long, jDialog: JDialog, dialogArea: TextArea) extends JavaInterMsg
}
