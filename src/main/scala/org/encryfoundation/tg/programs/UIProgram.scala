package org.encryfoundation.tg.programs

import cats.effect.concurrent.{MVar, Ref}
import cats.effect.{Concurrent, Sync, Timer}
import cats.implicits._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.TextArea
import org.drinkless.tdlib.TdApi.{MessagePhoto, MessageText, MessageVideo}
import org.drinkless.tdlib.{ClientUtils, TdApi}
import org.encryfoundation.tg.AuthRequestHandler
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.handlers.{EmptyHandler, PrivateGroupChatCreationHandler, ValueHandler}
import org.encryfoundation.tg.javaIntegration.BackMsg
import org.encryfoundation.tg.javaIntegration.BackMsg._
import org.encryfoundation.tg.javaIntegration.FrontMsg.{HistoryMsgs, NewMsgsInChat}
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.services.{ClientService, PrivateConferenceService, UserStateService}
import org.encryfoundation.tg.userState.UserState
import org.encryfoundation.tg.utils.{ChatUtils, MessagesUtils}
import org.javaFX.model.nodes.{VBoxDialogTextMessageCell, VBoxMessageCell}
import org.javaFX.model.{JDialog, JMessage}
import scorex.crypto.encode.Base64

import scala.collection.JavaConverters._
import scala.util.{Random, Try}

trait UIProgram[F[_]] {

  def run(): Stream[F, Unit]
}

object UIProgram {

  private class Live[F[_]: Concurrent: Timer: Logger](userStateRef: Ref[F, UserState[F]],
                                                      privateConfService: PrivateConferenceService[F],
                                                      userStateService: UserStateService[F],
                                                      clientService: ClientService[F],
                                                      dialogAreaRef: MVar[F, TextArea],
                                                      jDialogRef: MVar[F, JDialog]) extends UIProgram[F] {

    def processLastMessage(msg: TdApi.Message, state: UserState[F]): F[VBoxMessageCell] =
      MessagesUtils.getSender(msg, userStateService).flatMap( sender =>
        userStateService.getPrivateGroupChat(msg.chatId).map {
          case Some(privateGroupChat) =>
            msg.content match {
              case text: MessageText =>
                val aes = AESEncryption(privateGroupChat.password.getBytes())
                val msgText = Try(state.users.get(msg.senderUserId)
                  .map(_.phoneNumber)
                  .getOrElse("Unknown sender") + ": " +
                  aes.decrypt(Base64.decode(text.text.text).get).map(_.toChar).mkString).getOrElse("Unknown msg")
                new VBoxDialogTextMessageCell(new JMessage[String](msg.isOutgoing, msgText, msg.date.toString, sender, false, msg.id))
              case _ =>
                val msgText = state.users.get(msg.senderUserId).map(_.phoneNumber).getOrElse("Unknown sender") + ": Unknown msg type"
                new VBoxDialogTextMessageCell(new JMessage[String](msg.isOutgoing, msgText, msg.date.toString, sender, false, msg.id))
            }
          case None =>
            msg.content match {
              case text: MessageText =>
                val msgText = state.users.get(msg.senderUserId).map(_.phoneNumber).getOrElse("Unknown sender") + ": " + text.text.text
                new VBoxDialogTextMessageCell(new JMessage[String](msg.isOutgoing, msgText, msg.date.toString, sender, false, msg.id))
              case _: MessagePhoto =>
                val msgText = state.users.get(msg.senderUserId).map(_.phoneNumber).getOrElse("Unknown sender") + ": " + "photo"
                new VBoxDialogTextMessageCell(new JMessage[String](msg.isOutgoing, msgText, msg.date.toString, sender, false, msg.id))
              case _: MessageVideo =>
                val msgText = state.users.get(msg.senderUserId).map(_.phoneNumber).getOrElse("Unknown sender") + ": " + "video"
                new VBoxDialogTextMessageCell(new JMessage[String](msg.isOutgoing, msgText, msg.date.toString, sender, false, msg.id))
              case _ =>
                val msgText = state.users.get(msg.senderUserId).map(_.phoneNumber).getOrElse("Unknown sender") + ": Unknown msg type"
                new VBoxDialogTextMessageCell(new JMessage[String](msg.isOutgoing, msgText, msg.date.toString, sender, false, msg.id))
            }
        }
      )

    def processMsg(msg: BackMsg): F[Unit] = msg match {
      case _@SetActiveChat(chatId) =>
        for {
          state <- userStateRef.get
          _ <- clientService.sendRequest(new TdApi.CloseChat(state.activeChat))
          _ <- clientService.sendRequest(new TdApi.OpenChat(chatId))
          msgs <- ChatUtils.getMsgs(chatId, 20, clientService, state, userStateService)
          _ <- userStateRef.update(_.copy(activeChat = chatId))
          _ <- clientService.sendRequest(new TdApi.ViewMessages(chatId, msgs.map(_.getElement.getId).toArray, false), EmptyHandler[F]())
          _ <- state.javaState.get().inQueue.put(NewMsgsInChat(msgs.asJava)).pure[F]
        } yield ()
      case _@SendToChat(msg) =>
        userStateRef.get.flatMap( state =>
          ClientUtils.sendMsg(state.chatList.find(_.id == state.activeChat).get, msg, userStateRef, clientService)
        )
      case _@CreateCommunityJava(name, usersJava) =>
        privateConfService.createConference(name, "me" :: usersJava.asScala.toList)
      case _@CreatePrivateGroupChat(name) =>
        for {
          state <- userStateRef.get
          communityBytes <- state.db.get(PrivateConferenceService.confInfo(name))
          _ <- communityBytes match {
            case Some(bytes) =>
              val community = PrivateCommunity.parseBytes(bytes).get
              createGroup(
                name + "Chat",
                name,
                Random.nextLong().toString,
                community.users.tail.map(_.userTelegramLogin)
              )
            case None => Sync[F].delay(println("Got nothing"))
          }
        } yield ()
      case _@SetPhone(phone) =>
        userStateRef.get.flatMap(state =>
          clientService.sendRequest(new TdApi.SetAuthenticationPhoneNumber(phone, null), AuthRequestHandler(userStateRef))
        ) >> Logger[F].info("Set phone")
      case _@SetPass(pass) =>
        userStateRef.get.flatMap(state =>
          clientService.sendRequest(new TdApi.CheckAuthenticationPassword(pass), AuthRequestHandler(userStateRef))
        ) >> Logger[F].info("Set pass")
      case _@SetVCCode(vcCode) =>
        userStateRef.get.flatMap(state =>
          clientService.sendRequest(new TdApi.CheckAuthenticationCode(vcCode), AuthRequestHandler(userStateRef))
        ) >> Logger[F].info(s"Set code ${vcCode}")
      case _@DeleteCommunity(name) =>
        privateConfService.deleteConference(name) >> userStateService.deleteCommunity(name)
      case _@Logout() =>
        userStateRef.get.flatMap(state =>
          clientService.sendRequest(new TdApi.LogOut)
        ) >> Logger[F].info("Logout")
      case _@LoadNextChatsChunk(qty) =>
        clientService.sendRequest(
          new TdApi.GetChats(new TdApi.ChatListMain(), Long.MaxValue, 0, qty + 20),
          EmptyHandler[F]()
        ) >> userStateService.increaseChatLimit(qty + 20)
      case _@LoadNextMsgsChunk(currentQty) =>
        for {
          state <- userStateRef.get
          msgs <- ChatUtils.getMsgs(state.activeChat, currentQty + 20, clientService, state, userStateService)
          _ <- clientService.sendRequest(new TdApi.ViewMessages(state.activeChat, msgs.map(_.getElement.getId).toArray, false), EmptyHandler[F]())
          _ <- state.javaState.get().inQueue.put(HistoryMsgs(msgs.asJava)).pure[F]
        } yield ()
    }

    private def createGroup(groupname: String,
                            conferenceName: String,
                            password: String,
                            users: List[String]): F[Unit] = {
      for {
        state <- userStateRef.get
        userIds <- Concurrent[F].delay(users.flatMap(
          username => state.users.find(userInfo => userInfo._2.username == username || userInfo._2.phoneNumber == username)
        ))
        _ <- Logger[F].info(s"Create private group chat for conference ${conferenceName} with next group: ${groupname} " +
          s"and users(${users}):")
        confInfo <- privateConfService.findConf(conferenceName)

        _ <- clientService.sendRequest(
          new TdApi.CreateNewBasicGroupChat(userIds.map(_._1).toArray, groupname),
          PrivateGroupChatCreationHandler[F](
            userStateRef,
            confInfo,
            groupname,
            userIds.map(_._2),
            confInfo.users.head.userTelegramLogin,
            password
          )(privateConfService, userStateService, clientService)
        )
        _ <- state.db.put(Database.privateGroupChatsKey, groupname.getBytes())
        _ <- state.db.put(groupname.getBytes(), password.getBytes())
      } yield ()
    }

    override def run: Stream[F, Unit] = (for {
      state <- Stream.eval(userStateRef.get)
      queue <- Stream.emit(state.javaState.get().outQueue)
      elem <- Stream.emit(queue.take())
      _ <- Stream.eval(processMsg(elem))
    } yield ()).repeat
  }

  def apply[F[_]: Concurrent: Timer: Logger](userStateRef: Ref[F, UserState[F]],
                                             privateConfService: PrivateConferenceService[F],
                                             userStateService: UserStateService[F],
                                             clientService: ClientService[F]): F[UIProgram[F]] =
    for {
      dialogAreaMVar <- MVar.empty[F, TextArea]
      jDialogMVar <- MVar.empty[F, JDialog]
    } yield new Live(userStateRef, privateConfService, userStateService, clientService, dialogAreaMVar, jDialogMVar)
}
