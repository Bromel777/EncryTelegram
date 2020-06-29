package org.encryfoundation.tg.javaIntegration

trait AuthMsg {
  def code: Int
}
object AuthMsg {

  val loadVC = LoadVCWindow
  val loadPass = LoadPassWindow
  val loadChats = LoadChatsWindow

  object LoadVCWindow extends AuthMsg {
    def code: Int = 0
  }
  object LoadPassWindow extends AuthMsg {
    def code: Int = 1
  }
  object LoadChatsWindow extends AuthMsg {
    def code: Int = 2
  }
}
