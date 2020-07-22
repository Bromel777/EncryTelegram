package org.encryfoundation.tg.pipelines.groupVerification

import cats.Applicative
import cats.effect.concurrent.{MVar, Ref}
import cats.effect.{Concurrent, Timer}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import it.unisa.dia.gas.jpbc.Element
import org.drinkless.tdlib.{Client, ClientUtils, TdApi}
import org.encryfoundation.mitmImun.Prover
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.handlers.CloseChatHandler
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.pipelines.messages.StepMsg
import org.encryfoundation.tg.pipelines.messages.StepMsg.GroupVerificationStepMsg.{ProverThirdStepMsg, VerifierSecondStepMsg}
import org.encryfoundation.tg.pipelines.messages.StepMsg.{EndPipeline, StartPipeline}
import org.encryfoundation.tg.pipelines.messages.serializer.EndPipelineMsgSerializer._
import org.encryfoundation.tg.pipelines.messages.serializer.StartPipelineMsgSerializer._
import org.encryfoundation.tg.pipelines.messages.serializer.StepMsgSerializer
import org.encryfoundation.tg.pipelines.messages.serializer.groupVerification.ProverThirdMsgSerializer._
import org.encryfoundation.tg.services.{ClientService, UserStateService}
import org.encryfoundation.tg.userState.{PrivateGroupChat, UserState}
import scorex.crypto.encode.Base64
import scorex.crypto.hash.Blake2b256

case class ProverThirdStep[F[_]: Concurrent: Timer: Logger](prover: Prover,
                                                            community: PrivateCommunity,
                                                            recipientLogin: String,
                                                            chatPass: String,
                                                            userState: Ref[F, UserState[F]],
                                                            secretChat: TdApi.Chat,
                                                            chatId: Long,
                                                            firstStep: Element,
                                                            privateGroupChat: PrivateGroupChat,
                                                            verifierSecondStepMsg: MVar[F, VerifierSecondStepMsg])(
                                                            userStateService: UserStateService[F],
                                                            clientService: ClientService[F]
                                                            ) extends Pipeline[F] {

  private def send2Chat[M <: StepMsg](msg: M)(implicit s: StepMsgSerializer[M]): F[Unit] =
    ClientUtils.sendMessage(
      chatId,
      Base64.encode(StepMsgSerializer.toBytes(msg)),
      clientService
    )

  def processPreviousStepStart: F[Pipeline[F]] = Applicative[F].pure(this)

  def processPreviousStepEnd: F[Pipeline[F]] = for {
    state <- userState.get
    _ <- send2Chat(StartPipeline(ProverThirdStep.pipelineName))
    secondStep <- verifierSecondStepMsg.read
    thirdStep <- prover.thirdStep(secondStep.secondStep).pure[F]
    commonKey <- prover.produceCommonKey(secondStep.verifierPubKey1, firstStep, secondStep.secondStep).pure[F]
    aes <- AESEncryption(Blake2b256.hash(commonKey)).pure[F]
    cypherdGroup <- privateGroupChat.copy(
      communityName = Base64.encode(aes.encrypt(privateGroupChat.communityName.getBytes())),
      groupName = Base64.encode(aes.encrypt(privateGroupChat.groupName.getBytes())),
      password = Base64.encode(aes.encrypt(privateGroupChat.password.getBytes()))
    ).pure[F]
    _ <- send2Chat(
      ProverThirdStepMsg(
        thirdStep,
        privateGroupChat.chatId,
        Base64.encode(aes.encrypt(community.name.getBytes)),
        cypherdGroup
      )
    )
    _ <- send2Chat(EndPipeline(ProverThirdStep.pipelineName))
    _ <- clientService.sendRequest(new TdApi.CloseChat(chatId), CloseChatHandler[F](userState, chatId))
  } yield this

  def processStepInput(input: StepMsg): F[Pipeline[F]] = input match {
    case msg: VerifierSecondStepMsg =>
      for {
        _ <- Logger[F].info(s"Receive: ${msg}")
        _ <- verifierSecondStepMsg.put(msg)
      } yield this
    case _ => Applicative[F].pure(this)
  }

  override def processInput(input: Array[Byte]): F[Pipeline[F]] =
    StepMsgSerializer.parseBytes(input) match {
      case Right(StartPipeline(pipelineName)) if pipelineName == VerifierSecondStep.pipeLineName  => processPreviousStepStart
      case Right(EndPipeline(pipelineName)) if pipelineName == VerifierSecondStep.pipeLineName => processPreviousStepEnd
      case Right(msg) => processStepInput(msg)
      case Left(err) =>
        Logger[F].error(s"Err during parsing step: ${err}. Input: ${input.map(_.toChar).mkString}") >> Applicative[F].pure(this)
    }
}

object ProverThirdStep {
  val pipelineName = "proverThirdStep"
}
