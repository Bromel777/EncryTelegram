package org.encryfoundation.tg.utils

import java.text.SimpleDateFormat

import cats.Applicative
import cats.data.OptionT
import cats.effect.Sync
import org.drinkless.tdlib.TdApi
import org.drinkless.tdlib.TdApi.{MessagePhoto, MessageText, MessageVideo}
import org.encryfoundation.tg.services.UserStateService
import org.javaFX.model.JMessage
import org.javaFX.model.nodes.VBoxDialogTextMessageCell
import cats.implicits._
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.{EndPipeline, StartPipeline}
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializer
import org.encryfoundation.tg.pipelines.groupVerification.{ProverFirstStep, ProverThirdStep, VerifierSecondStep}
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StartPipelineMsgSerializer._
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.EndPipelineMsgSerializer._
import scorex.crypto.encode.Base64

object MessagesUtils {

  val pipelinesStartMsg: List[String] =
    List(ProverFirstStep.pipelineName, ProverThirdStep.pipelineName, VerifierSecondStep.pipeLineName)
      .map(name => Base64.encode(StepMsgSerializer.toBytes(StartPipeline(name))))

  val pipelinesEndMsg: List[String] =
    List(ProverFirstStep.pipelineName, ProverThirdStep.pipelineName, VerifierSecondStep.pipeLineName)
      .map(name => Base64.encode(StepMsgSerializer.toBytes(EndPipeline(name))))

  def processMessage(msg: TdApi.Message): String =
    if (msg == null) "No message"
    else msg.content match {
      case text: MessageText => text.text.text
      case _: MessagePhoto => "Unsupported msg type"
      case _: MessageVideo => "Unsupported msg type"
      case _ => "Unknown msg type"
    }

  def getLastMessageTime(msg: TdApi.Message): Long =
    if (msg == null) 0L else msg.date.toLong

  def getSender[F[_]: Sync](msg: TdApi.Message, userStateService: UserStateService[F]): F[String] =
    if (!msg.isOutgoing) {
      OptionT(userStateService.getUserById(msg.senderUserId)).map { user =>
        val firstNameWithLast = user.firstName ++ " " ++ user.lastName
        val phone = user.phoneNumber
        val login = user.username
        if (!firstNameWithLast.isEmpty) firstNameWithLast
        else if (!phone.isEmpty) phone
        else login
      }.getOrElse("Unknown user")
    } else Applicative[F].pure("Me")
}
