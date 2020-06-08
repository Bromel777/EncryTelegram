package org.encryfoundation.tg.utils

import cats.Monad
import cats.data.OptionT
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.TdApi
import org.encryfoundation.tg.userState.UserState
import cats.implicits._

object UserStateUtils {

  def findUserByIdentifier[F[_]: Monad](userIdentifier: String,
                                        stateRef: Ref[F, UserState[F]]): OptionT[F, TdApi.User] = for {
    state <- OptionT.liftF(stateRef.get)
    possibleUser <- OptionT.fromOption[F](state.users.find { case (_, user) =>
      user.username == userIdentifier ||
      user.phoneNumber == userIdentifier ||
      s"${user.firstName} ${user.lastName}" == userIdentifier
    }.map(_._2))
  } yield possibleUser

  def findChatByIdentifier[F[_]: Monad](chatIdentifier: String,
                                        stateRef: Ref[F, UserState[F]]): OptionT[F, TdApi.Chat] = for {
    state <- OptionT.liftF(stateRef.get)
    possibleChat <- OptionT.fromOption[F](state.chatList.find { chat =>
      chat.title == chatIdentifier
    })
  } yield possibleChat
}
