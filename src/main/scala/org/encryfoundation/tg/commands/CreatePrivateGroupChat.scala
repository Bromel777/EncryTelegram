package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Resource, Sync, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.ChatCreationHandler
import org.encryfoundation.tg.leveldb.Database

case class CreatePrivateGroupChat[F[_]: Concurrent: Timer: Logger](client: Client[F],
                                                                   userStateRef: Ref[F, UserState[F]],
                                                                   db: Database[F]) extends Command[F] {

  override val name: String = "createPrivateGroupChat"

  override def run(args: List[String]): F[Unit] = for {
    _ <- createGroup(
      userStateRef,
      client,
      args.head,
      args.tail.head,
      args.drop(2)
    )
  } yield ()

  def createGroup(stateRef: Ref[F, UserState[F]],
                  client: Client[F],
                  groupname: String,
                  password: String,
                  users: List[String]): F[Unit] = {
    for {
      state <- stateRef.get
      userIds <- Concurrent[F].delay(users.flatMap(username => state.users.find(_._2.username == username)))
      _ <- Logger[F].info(s"Create private chat with next group: ${groupname}")
      _ <- client.send(
        new TdApi.CreateNewBasicGroupChat(userIds.map(_._1).toArray, groupname),
        ChatCreationHandler[F](stateRef, password)
      )
      _ <- db.put(Database.privateGroupChatsKey, groupname.getBytes())
      _ <- db.put(groupname.getBytes(), password.getBytes())
    } yield ()
  }
}
