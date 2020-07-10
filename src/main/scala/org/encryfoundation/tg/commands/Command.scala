package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.Client
import org.encryfoundation.tg.errors.TdError
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.services.{ClientService, PrivateConferenceService, UserStateService}
import org.encryfoundation.tg.userState.UserState
import tofu.Raise

trait Command[F[_]] {

  val name: String

  def run(args: List[String]): F[Unit]
}

object Command {
  def getCommands[F[_]: Concurrent: Timer: Logger: Raise[*[_], TdError]](userStateRef: Ref[F, UserState[F]],
                                                                         db: Database[F])(
                                                                         confService: PrivateConferenceService[F],
                                                                         userStateService: UserStateService[F],
                                                                         clientService: ClientService[F]
                                                                         ): List[Command[F]] = List(
    CreatePrivateGroupChat[F](userStateRef, db)(confService, userStateService, clientService),
    PrintChats[F](userStateRef, db),
    ReadChat[F](userStateRef, db)(userStateService, clientService),
    SendToChat[F](userStateRef, clientService),
    CreatePrivateConference[F](userStateRef, db)(confService),
    ShowPrivateConferences[F](userStateRef, db)(confService),
    CreatePrivateChat[F](clientService, userStateRef),
    Logout[F](clientService, userStateRef, db),
    CloseChat[F](userStateRef, db)(clientService),
    CloseSecretChat[F](clientService, userStateRef, db)
  )
}
