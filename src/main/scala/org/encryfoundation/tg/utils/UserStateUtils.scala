package org.encryfoundation.tg.utils

import cats.Monad
import cats.data.OptionT
import cats.effect.Sync
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.TdApi
import org.encryfoundation.tg.userState.UserState
import cats.implicits._

import scala.util.Try

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
      chat.title == chatIdentifier || Try(chatIdentifier.toLong).exists(_ == chat.id)
    })
  } yield possibleChat

  def getPhoneNumber[F[_]: Sync](stateRef: Ref[F, UserState[F]]): F[String] = for {
    state <- stateRef.get
    javaState <- state.javaState.get().pure[F]
    number <- if (javaState.userInfo.get(0) == null) getPhoneNumber(stateRef)
              else javaState.userInfo.get(0).pure[F]
  } yield number

  def getVC[F[_]: Monad](stateRef: Ref[F, UserState[F]]): F[String] = for {
    state <- stateRef.get
    javaState <- state.javaState.get().pure[F]
    vc <- if (javaState.userInfo.get(1) == null) getVC(stateRef)
    else javaState.userInfo.get(1).pure[F]
  } yield vc

  def getPass[F[_]: Monad](stateRef: Ref[F, UserState[F]]): F[String] = for {
    state <- stateRef.get
    javaState <- state.javaState.get().pure[F]
    pass <- if (javaState.userInfo.get(2) == null) getPass(stateRef)
    else javaState.userInfo.get(2).pure[F]
  } yield pass
}
