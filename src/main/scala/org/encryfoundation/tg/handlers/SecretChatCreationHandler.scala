package org.encryfoundation.tg.handlers

import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.{ResultHandler, TdApi}
import org.encryfoundation.tg.userState.UserState
import cats.implicits._

case class SecretChatCreationHandler[F[_]: Concurrent: Logger](stateRef: Ref[F, UserState[F]]) extends ResultHandler[F]{
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Chat.CONSTRUCTOR =>
      for {
        state <- stateRef.get
        _ <- Logger[F].info(s"Receive secret: ${obj}")
        _ <- stateRef.update(
          _.copy(
            mainChatList = state.mainChatList + (obj.asInstanceOf[TdApi.Chat].id -> obj.asInstanceOf[TdApi.Chat]),
          )
        )
      } yield ()
    case _ => ().pure[F]
  }
}
