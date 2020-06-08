package org.encryfoundation.tg.handlers

import cats.effect.{Concurrent, Sync}
import org.drinkless.tdlib.{ResultHandler, TdApi}
import org.drinkless.tdlib.TdApi.{MessageText, Messages}
import org.encryfoundation.tg.crypto.AESEncryption
import scorex.crypto.encode.Base16
import cats.syntax.applicative._
import scala.util.Try

case class MessagesHandler[F[_]: Concurrent](password: Option[String]) extends ResultHandler[F] {

  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Messages.CONSTRUCTOR =>
      val messages = obj.asInstanceOf[Messages]
      val msgs = password.map { pass =>
        val aes = AESEncryption(pass.getBytes())
        messages.messages.map {
          _.content match {
            case txtMsg: MessageText =>
              Try(aes.decrypt(Base16.decode(txtMsg.text.text).get).map(_.toChar).mkString)
                .getOrElse(txtMsg.text.text)
            case _ => "Unknown msg!"
          }
        }
      } getOrElse(Array.empty[String])
      Sync[F].delay(println("Messages:" + msgs.mkString("\n ")))
    case TdApi.Message.CONSTRUCTOR =>
      Sync[F].delay(println("Receive message"))
    case _ => ().pure[F]
  }
}
