package org.drinkless.tdlib

import cats.effect.Sync
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.Client123.ResultHandler
import org.encryfoundation.tg.userState.UserState
import cats.implicits._

case class DummyHandler[F[_]: Sync](client: Client[F],
                                    ref: Ref[F, UserState[F]],
                                    getChats: (Client[F], Ref[F, UserState[F]]) => F[Unit]) extends ResultHandler[F] {
  /**
   * Callback called on result of query to TDLib or incoming update from TDLib.
   *
   * @param obj Result of query or update of type TdApi.Update about new events.
   */
  override def onResult(obj: TdApi.Object): F[Unit] = obj.getConstructor match {
    case TdApi.Error.CONSTRUCTOR =>
      Sync[F].delay(println("Err occured"))
    case TdApi.Chats.CONSTRUCTOR =>
      val chatIds = obj.asInstanceOf[TdApi.Chats].chatIds
      ().pure[F]
//      if (chatIds.isEmpty) {
//        Sync[F].delay(println("All!"))
//      } else Sync[F].delay(println(s"Receive ${obj}")) >> getChats(client, ref)
    case _ =>
      Sync[F].delay(println("Exit"))
  }
}
