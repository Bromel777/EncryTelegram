package org.encryfoundation.tg.utils

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.MVar
import org.drinkless.tdlib.TdApi
import org.encryfoundation.tg.services.{ClientService, UserStateService}
import org.javaFX.model.nodes.{VBoxDialogTextMessageCell, VBoxMessageCell}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.TdApi.{MessagePhoto, MessageText, MessageVideo}
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.handlers.ValueHandler
import org.encryfoundation.tg.userState.UserState
import org.javaFX.model.JMessage
import scorex.crypto.encode.Base64

import scala.concurrent.duration._
import scala.util.Try

object ChatUtils {

  def processLastMessage[F[_]: Sync](msg: TdApi.Message, state: UserState[F], userStateService: UserStateService[F]): F[VBoxMessageCell] =
    MessagesUtils.getSender(msg, userStateService).flatMap( sender =>
      MessagesUtils.decryptMsg(msg, state)(userStateService).map { msg =>
        val msgText = MessagesUtils.tdMsg2String(msg)
        new VBoxDialogTextMessageCell(new JMessage[String](msg.isOutgoing, msgText, msg.date.toString, sender, false, msg.id))
      }
    )

  def getUnreadMsgsCount(chat: TdApi.Chat): Int = if (chat.unreadCount == null) 0 else chat.unreadCount

  def getMsgs[F[_]: Concurrent: Timer: Logger](chatId: Long, limit: Int,
                                               clientService: ClientService[F],
                                               userState: UserState[F],
                                               userStateService: UserStateService[F]): F[List[VBoxMessageCell]] = {
    def recurGet(state: List[VBoxMessageCell], attempts: Int): F[List[VBoxMessageCell]] =
      for {
        msgsMVar <- MVar.empty[F, List[VBoxMessageCell]]
        _ <- clientService.sendRequest(
          new TdApi.GetChatHistory(chatId, 0, 0, limit, false),
          ValueHandler(
            msgsMVar,
            (msg: TdApi.Messages) =>
              msg.messages.toList.traverse(processLastMessage(_, userState, userStateService)).map(_.reverse))
        )
        msgs <- msgsMVar.read
        resultAccum <-
          if (msgs.length >= limit) msgs.pure[F]
          else if (attempts == 10) Logger[F].info(s"Attempts: ${attempts}") >> msgs.pure[F]
          else Timer[F].sleep(100.milliseconds) >> recurGet(msgs, attempts + 1)
      } yield resultAccum

    recurGet(List.empty, 0)
  }
}
