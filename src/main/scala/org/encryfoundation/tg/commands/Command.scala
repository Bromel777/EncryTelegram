package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.Client
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState

trait Command[F[_]] {

  val name: String

  def run(args: List[String]): F[Unit]
}

object Command {
  def getCommands[F[_]: Concurrent: Timer](client: Client[F],
                                           userStateRef: Ref[F, UserState[F]],
                                           db: Database[F]): List[Command[F]] = List(
    CreatePrivateGroupChat[F](client, userStateRef, db),
    PrintChats[F](client, userStateRef, db),
    ReadChat[F](client, userStateRef, db),
    SendTo[F](client, userStateRef),
    WriteSecure[F](client, userStateRef, db),
    //CreatePrivateConference[F](client, userStateRef, db)
  )
}
