package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.Client
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.services.PrivateConferenceService
import org.encryfoundation.tg.userState.UserState

trait Command[F[_]] {

  val name: String

  def run(args: List[String]): F[Unit]
}

object Command {
  def getCommands[F[_]: Concurrent: Timer: Logger](client: Client[F],
                                                   userStateRef: Ref[F, UserState[F]],
                                                   db: Database[F])(
                                                   confService: PrivateConferenceService[F]
                                                   ): List[Command[F]] = List(
    CreatePrivateGroupChat[F](client, userStateRef, db)(confService),
    PrintChats[F](client, userStateRef, db),
    ReadChat[F](client, userStateRef, db),
    SendToChat[F](client, userStateRef),
    WriteSecure[F](client, userStateRef, db),
    CreatePrivateConference[F](client, userStateRef, db)(confService),
    ShowPrivateConferences[F](client, userStateRef, db)(confService),
    CreatePrivateChat[F](client, userStateRef),
    SendToSecretChat[F](client, userStateRef, db),
    Logout[F](client, userStateRef, db),
    CloseChat[F](client, userStateRef, db),
    CloseSecretChat[F](client, userStateRef, db)
  )
}
