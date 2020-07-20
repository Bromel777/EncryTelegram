package org.encryfoundation.tg.pipelines.utilPipes

import cats.effect.{Concurrent, Timer}
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.services.{ClientService, UserStateService}

class WelcomePipe[F[_]: Concurrent: Timer: Logger](userStateService: UserStateService[F],
                                                   clientService: ClientService[F]) extends Pipeline[F] {

  override def processInput(input: Array[Byte]): F[Pipeline[F]] = ???
}
