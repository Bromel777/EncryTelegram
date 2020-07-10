package org.encryfoundation.tg.handlers

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{Client, ResultHandler, TdApi}
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import org.encryfoundation.tg.services.ClientService

case class CloseChatHandler[F[_]: Concurrent: Timer: Logger](stateRef: Ref[F, UserState[F]],
                                                             chatId: Long) extends ResultHandler[F] {

  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Ok.CONSTRUCTOR =>
      for {
        _ <- stateRef.update { prevState =>
          prevState.copy(
            mainChatList = prevState.mainChatList - chatId,
            chatIds = prevState.chatIds - chatId,
            chatList = prevState.chatList.filter(_.id != chatId)
          )
        }
      } yield ()
    case err => Logger[F].info(s"Err during chat close: ${obj}")
  }
}
