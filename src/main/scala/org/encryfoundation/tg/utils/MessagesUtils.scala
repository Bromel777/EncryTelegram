package org.encryfoundation.tg.utils

import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

import cats.Applicative
import cats.data.OptionT
import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.TdApi
import org.drinkless.tdlib.TdApi.{MessageBasicGroupChatCreate, MessagePhoto, MessageText, MessageVideo}
import org.encryfoundation.tg.services.{ClientService, UserStateService}
import org.javaFX.model.JMessage
import org.javaFX.model.nodes.{VBoxDialogTextMessageCell, VBoxMessageCell}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.pipelines.Pipelines
import org.encryfoundation.tg.pipelines.messages.StepMsg.{EndPipeline, StartPipeline}
import org.encryfoundation.tg.pipelines.messages.serializer.StepMsgSerializer
import org.encryfoundation.tg.pipelines.groupVerification.{ProverFirstStep, ProverThirdStep, VerifierSecondStep}
import org.encryfoundation.tg.pipelines.messages.serializer.StartPipelineMsgSerializer._
import org.encryfoundation.tg.pipelines.messages.serializer.EndPipelineMsgSerializer._
import org.encryfoundation.tg.pipelines.utilPipes.{EmptyPipeline, WelcomeInitPipe, WelcomeProcessPipe}
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base64

import scala.util.{Failure, Success, Try}

object MessagesUtils {

  val pipelinesStartMsg: List[String] =
    List(ProverFirstStep.pipelineName, ProverThirdStep.pipelineName, VerifierSecondStep.pipeLineName)
      .map(name => Base64.encode(StepMsgSerializer.toBytes(StartPipeline(name)))) :+ WelcomeInitPipe.welcomeMsgText

  val pipelinesEndMsg: List[String] =
    List(ProverFirstStep.pipelineName, ProverThirdStep.pipelineName, VerifierSecondStep.pipeLineName)
      .map(name => Base64.encode(StepMsgSerializer.toBytes(EndPipeline(name))))

  def tdMsg2String(msg: TdApi.Message): String =
    if (msg == null) "No message"
    else msg.content match {
      case text: MessageText => text.text.text
      case _: MessagePhoto => "Unsupported msg type: Photo"
      case _: MessageVideo => "Unsupported msg type: Video"
      case groupChatCreation: MessageBasicGroupChatCreate => s"created the group ${groupChatCreation.title}"
      case msg => s"Unknown msg type."
    }

  def decryptMsg[F[_]: Sync](msg: TdApi.Message, state: UserState[F])
                            (userStateService: UserStateService[F]): F[TdApi.Message] =
    userStateService.getPrivateGroupChat(msg.chatId).map {
      case Some(privateGroupChat) =>
        msg.content match {
          case text: MessageText =>
            val aes = AESEncryption(privateGroupChat.password.getBytes())
            val msgText = Try(state.users.get(msg.senderUserId)
              .map(_.phoneNumber)
              .getOrElse("Unknown sender") + ": " +
              new String(aes.decrypt(Base64.decode(text.text.text).get), StandardCharsets.UTF_8)).getOrElse("Unknown msg")
            val newText = text
            newText.text.text = msgText
            msg.content = newText
            msg
          case _ => msg
        }
      case None => msg
    }

  def msg2VBox[F[_]: Sync](msg: TdApi.Message, state: UserState[F])
                          (userStateService: UserStateService[F]): F[VBoxMessageCell] =
    decryptMsg(msg, state)(userStateService).flatMap { decryptedMsg =>
      MessagesUtils.getSender(msg, userStateService).map { sender =>
        val msgText = tdMsg2String(decryptedMsg)
        new VBoxDialogTextMessageCell(
          new JMessage[String](msg.isOutgoing, msgText, msg.date.toString, sender, false, msg.id)
        )
      }
    }

  def processTdMsg[F[_]: Concurrent: Timer: Logger](msg: TdApi.Message,
                                                    userStateRef: Ref[F, UserState[F]])
                                                   (userStateService: UserStateService[F],
                                                    clientService: ClientService[F]): F[Unit] =
    if (isPipelineMsg(msg)) processPipelineMsg(msg, userStateRef)(userStateService, clientService)
    else processNotPipelineMsg(msg, userStateRef)(userStateService, clientService)

  def processNotPipelineMsg[F[_]: Concurrent: Timer: Logger](msg: TdApi.Message,
                                                             userStateRef: Ref[F, UserState[F]])
                                                            (userStateService: UserStateService[F],
                                                             clientService: ClientService[F]): F[Unit] =
    for {
      state <- userStateRef.get
      newmsg <- msg2VBox(msg, state)(userStateService)
      _ <- if (msg.chatId == state.activeChat) Sync[F].delay {
        val javaState = state.javaState.get()
        val localDialogHistory = javaState.messagesListView
        val newMessageView = localDialogHistory.getItems
        newMessageView.add(newmsg)
        localDialogHistory.setItems(newMessageView)
      } else Applicative[F].pure(())
    } yield ()

  def isPipelineMsg[F[_]: Sync](msg: TdApi.Message): Boolean =
    msg.content match {
      case text: MessageText if Base64.decode(text.text.text).isSuccess &&
        StepMsgSerializer.parseBytes(Base64.decode(text.text.text).get).isRight => true
      case text: MessageText if text.text.text == WelcomeInitPipe.welcomeMsgText => true
      case _ => false
    }

  def processPipelineMsg[F[_]: Concurrent: Timer: Logger](msg: TdApi.Message,
                                                          userState: Ref[F, UserState[F]])
                                                         (userStateService: UserStateService[F],
                                                          clientService: ClientService[F]): F[Unit] =
    msg.content match {
      case a: MessageText =>
        Base64.decode(a.text.text) match {
          case Success(value) =>
            StepMsgSerializer.parseBytes(value) match {
              case Right(stepMsg) if !msg.isOutgoing =>
                userStateService.getPipeline(msg.chatId) flatMap {
                  case Some(pipeline) => for {
                    newPipeLine <- pipeline.processInput(value)
                    _ <- userStateService.updatePipelineChat(msg.chatId, newPipeLine)
                  } yield ()
                  case _ => Pipelines.findStart(
                    userState,
                    userStateService,
                    msg.chatId,
                    clientService,
                    stepMsg
                  ) >> Logger[F].info(s"No pipelines for msg: ${stepMsg}")
                }
              case Right(_) if a.text.text == WelcomeInitPipe.welcomeMsgText && !msg.isOutgoing =>
                //todo: Change input of hash func
                WelcomeProcessPipe.startPipeline(msg.chatId)(userStateService, clientService)
              case Left(_) if a.text.text == WelcomeInitPipe.welcomeMsgText && !msg.isOutgoing =>
                //todo: Change input of hash func
                WelcomeProcessPipe.startPipeline(msg.chatId)(userStateService, clientService)
              case Right(_) => processNotPipelineMsg(msg, userState)(userStateService, clientService)
              case Left(_) => processNotPipelineMsg(msg, userState)(userStateService, clientService)
            }
          case Failure(err) if a.text.text == WelcomeInitPipe.welcomeMsgText && !msg.isOutgoing =>
            //todo: Change input of hash func
            WelcomeProcessPipe.startPipeline(msg.chatId)(userStateService, clientService)
          case Failure(err) => processNotPipelineMsg(msg, userState)(userStateService, clientService)
        }
      case _ => processNotPipelineMsg(msg, userState)(userStateService, clientService)
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
