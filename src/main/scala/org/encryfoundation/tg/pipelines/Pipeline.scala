package org.encryfoundation.tg.pipelines

import cats.Applicative
import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.Client
import org.encryfoundation.tg.pipelines.groupVerification.{ProverFirstStep, VerifierSecondStep}
import org.encryfoundation.tg.pipelines.messages.StepMsg
import org.encryfoundation.tg.pipelines.messages.StepMsg.StartPipeline
import org.encryfoundation.tg.pipelines.messages.serializer.StartPipelineMsgSerializer._
import org.encryfoundation.tg.pipelines.messages.serializer.StepMsgSerializer
import org.encryfoundation.tg.services.{ClientService, UserStateService}
import org.encryfoundation.tg.userState.UserState

trait Pipeline[F[_]] {

  def processInput(input: Array[Byte]): F[Pipeline[F]]
}

trait HeadPipelineCompanion[F[_], T <: Pipeline[F]] {
  def startPipeline(state: Ref[F, UserState[F]],
                    userStateService: UserStateService[F],
                    chatId: Long,
                    clientService: ClientService[F],
                    msgBytes: Array[Byte]): F[Unit]
}

object Pipelines {
  def findStart[F[_]: Concurrent: Timer: Logger](userStateRef: Ref[F, UserState[F]],
                                                 userStateService: UserStateService[F],
                                                 chatId: Long,
                                                 clientService: ClientService[F],
                                                 msg: StepMsg): F[Unit] = msg match {
    case start@StartPipeline(pipelineName) if pipelineName == ProverFirstStep.pipelineName =>
      VerifierSecondStep.companion[F].startPipeline(
        userStateRef,
        userStateService,
        chatId,
        clientService,
        StepMsgSerializer.toBytes(start)
      )
    case _ => Applicative[F].pure(())
  }
}