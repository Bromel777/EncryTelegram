package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.Client
import org.encryfoundation.tg.errors.TdError
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.services.{PrivateConferenceService, UserStateService}
import org.encryfoundation.tg.userState.UserState
import tofu.Raise

trait Command[F[_]] {

  val name: String

  def run(args: List[String]): F[Unit]
}

object Command {
  def getCommands[F[_]: Concurrent: Timer: Logger: Raise[*[_], TdError]](client: Client[F],
                                                                         userStateRef: Ref[F, UserState[F]],
                                                                         db: Database[F])(
                                                                         confService: PrivateConferenceService[F],
                                                                         userStateService: UserStateService[F]
                                                                         ): List[Command[F]] = List(
    CreatePrivateGroupChat[F](client, userStateRef, db)(confService, userStateService),
    PrintChats[F](client, userStateRef, db),
    ReadChat[F](client, userStateRef, db),
    SendToChat[F](client, userStateRef),
    CreatePrivateConference[F](client, userStateRef, db)(confService),
    ShowPrivateConferences[F](client, userStateRef, db)(confService),
    CreatePrivateChat[F](client, userStateRef),
    Logout[F](client, userStateRef, db),
    CloseChat[F](client, userStateRef, db),
    CloseSecretChat[F](client, userStateRef, db)
  )
}
