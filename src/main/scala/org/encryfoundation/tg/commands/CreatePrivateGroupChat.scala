package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Resource, Sync, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.handlers.{PrivateGroupChatCreationHandler, SecretGroupPrivateChatCreationHandler}
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.services.{ClientService, PrivateConferenceService, UserStateService}

case class CreatePrivateGroupChat[F[_]: Concurrent: Timer: Logger](userStateRef: Ref[F, UserState[F]],
                                                                   db: Database[F])(
                                                                   privateConferenceService: PrivateConferenceService[F],
                                                                   userStateService: UserStateService[F],
                                                                   clientService: ClientService[F]) extends Command[F] {

  override val name: String = "createPrivateGroupChat"

  override def run(args: List[String]): F[Unit] = for {
    _ <- createGroup(
      userStateRef,
      clientService,
      args.head,
      args.tail.head,
      args.drop(2).head,
      args.drop(3)
    )
  } yield ()

  def createGroup(stateRef: Ref[F, UserState[F]],
                  clientService: ClientService[F],
                  groupname: String,
                  conferenceName: String,
                  password: String,
                  users: List[String]): F[Unit] = {
    for {
      state <- stateRef.get
      userIds <- Concurrent[F].delay(users.flatMap(
        username => state.users.find(userInfo => userInfo._2.username == username || userInfo._2.phoneNumber == username)
      ))
      _ <- Logger[F].info(s"Create private group chat for conference ${conferenceName} with next group: ${groupname} " +
        s"and users(${users}):")
      confInfo <- privateConferenceService.findConf(conferenceName)
      _ <- clientService.sendRequest(
        new TdApi.CreateNewBasicGroupChat(userIds.map(_._1).toArray, groupname),
        PrivateGroupChatCreationHandler[F](
          stateRef,
          confInfo,
          groupname,
          userIds.map(_._2),
          confInfo.users.head.userTelegramLogin,
          password
        )(privateConferenceService, userStateService, clientService)
      )
      _ <- db.put(Database.privateGroupChatsKey, groupname.getBytes())
      _ <- db.put(groupname.getBytes(), password.getBytes())
    } yield ()
  }
}
