package org.encryfoundation.tg.programs

import cats.Applicative
import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.{MVar, Ref}
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.{ListView, TextArea}
import org.drinkless.tdlib.{Client, ClientUtils, TdApi}
import org.drinkless.tdlib.TdApi.{MessagePhoto, MessageText, MessageVideo}
import org.encryfoundation.tg.AuthRequestHandler
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.handlers.{AccumulatorHandler, PrivateGroupChatCreationHandler, ValueHandler}
import org.encryfoundation.tg.javaIntegration.JavaInterMsg
import org.encryfoundation.tg.javaIntegration.JavaInterMsg.{CreateCommunityJava, CreatePrivateGroupChat, SendToChat, SetActiveChat, SetPass, SetPhone, SetVCCode}
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.services.{PrivateConferenceService, UserStateService}
import org.javaFX.model.{JDialog, JTextMessage}
import org.javaFX.model.nodes.VBoxMessageCell
import scorex.crypto.encode.Base64

import collection.JavaConverters._
import scala.util.Try

trait UIProgram[F[_]] {

  def run(): Stream[F, Unit]
}

object UIProgram {

  private class Live[F[_]: Concurrent: Timer: Logger](userStateRef: Ref[F, UserState[F]],
                                                      privateConfService: PrivateConferenceService[F],
                                                      userStateService: UserStateService[F],
                                                      client: Client[F],
                                                      dialogAreaRef: MVar[F, TextArea],
                                                      jDialogRef: MVar[F, JDialog]) extends UIProgram[F] {

    def processLastMessage(msg: TdApi.Message, state: UserState[F]): F[VBoxMessageCell] =
      userStateService.getPrivateGroupChat(msg.chatId).map {
        case Some(privateGroupChat) =>
          msg.content match {
            case text: MessageText =>
              val aes = AESEncryption(privateGroupChat.password.getBytes())
              val msgText = Try(state.users.get(msg.senderUserId)
                  .map(_.phoneNumber)
                  .getOrElse("Unknown sender") + ": " +
                  aes.decrypt(Base64.decode(text.text.text).get).map(_.toChar).mkString).getOrElse("Unkown msg")
              new VBoxMessageCell(new JTextMessage(msg.isOutgoing, msgText, msg.date.toString))
            case _ =>
              val msgText = state.users.get(msg.senderUserId).map(_.phoneNumber).getOrElse("Unknown sender") + ": Unknown msg type"
              new VBoxMessageCell(new JTextMessage(msg.isOutgoing, msgText, msg.date.toString))
          }
        case None =>
          msg.content match {
            case text: MessageText =>
              val msgText = state.users.get(msg.senderUserId).map(_.phoneNumber).getOrElse("Unknown sender") + ": " + text.text.text
              new VBoxMessageCell(new JTextMessage(msg.isOutgoing, msgText, msg.date.toString))
            case _: MessagePhoto =>
              val msgText = state.users.get(msg.senderUserId).map(_.phoneNumber).getOrElse("Unknown sender") + ": " + "photo"
              new VBoxMessageCell(new JTextMessage(msg.isOutgoing, msgText, msg.date.toString))
            case _: MessageVideo =>
              val msgText = state.users.get(msg.senderUserId).map(_.phoneNumber).getOrElse("Unknown sender") + ": " + "video"
              new VBoxMessageCell(new JTextMessage(msg.isOutgoing, msgText, msg.date.toString))
            case _ =>
              val msgText = state.users.get(msg.senderUserId).map(_.phoneNumber).getOrElse("Unknown sender") + ": Unknown msg type"
              new VBoxMessageCell(new JTextMessage(msg.isOutgoing, msgText, msg.date.toString))
          }
      }

    def processMsg(msg: JavaInterMsg): F[Unit] = msg match {
      case _@SetActiveChat(chatId) =>
        for {
          state <- userStateRef.get
          javaState <- state.javaState.get().pure[F]
          msgsMVar <- MVar.empty[F, List[VBoxMessageCell]]
          _ <- state.client.send(
            new TdApi.GetChatHistory(chatId, 0, 0, 20, false),
            ValueHandler(
              userStateRef,
              msgsMVar,
              (msg: TdApi.Messages) =>
                msg.messages.toList.traverse(processLastMessage(_, state)).map(_.reverse))
          )
          _ <- userStateRef.update(_.copy(activeChat = chatId))
          msgs <- msgsMVar.read
          _ <- Logger[F].info(s"msgs: ${msgs}")
          _ <- Sync[F].delay {
            val observList: ObservableList[VBoxMessageCell] = FXCollections.observableArrayList[VBoxMessageCell]()
            msgs.foreach(observList.add)
            javaState.messagesListView = new ListView[VBoxMessageCell]()
            javaState.messagesListView.setItems(observList)
            println("update after:" + javaState.messagesListView.getItems.toString)
          }
        } yield ()
      case _@SendToChat(msg) =>
        userStateRef.get.flatMap( state => ClientUtils.sendMsg(state.chatList.find(_.id == state.activeChat).get, msg, userStateRef))
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
                "1234",
                community.users.tail.map(_.userTelegramLogin)
              )
            case None => Sync[F].delay(println("Got nothing"))
          }
        } yield ()
      case _@SetPhone(phone) =>
        userStateRef.get.flatMap(state =>
          state.client.send(new TdApi.SetAuthenticationPhoneNumber(phone, null), AuthRequestHandler(userStateRef))
        ) >> Logger[F].info("Set phone")
      case _@SetPass(pass) =>
        userStateRef.get.flatMap(state =>
          state.client.send(new TdApi.CheckAuthenticationPassword(pass), AuthRequestHandler(userStateRef))
        ) >> Logger[F].info("Set pass")
      case _@SetVCCode(vcCode) =>
        userStateRef.get.flatMap(state =>
          state.client.send(new TdApi.CheckAuthenticationCode(vcCode), AuthRequestHandler(userStateRef))
        ) >> Logger[F].info("Set code")
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

        _ <- client.send(
          new TdApi.CreateNewBasicGroupChat(userIds.map(_._1).toArray, groupname),
          PrivateGroupChatCreationHandler[F](
            userStateRef,
            client,
            confInfo,
            groupname,
            userIds.map(_._2),
            confInfo.users.head.userTelegramLogin,
            password
          )(privateConfService, userStateService)
        )
        _ <- state.db.put(Database.privateGroupChatsKey, groupname.getBytes())
        _ <- state.db.put(groupname.getBytes(), password.getBytes())
      } yield ()
    }

    override def run: Stream[F, Unit] = (for {
      state <- Stream.eval(userStateRef.get)
      queue <- Stream.emit(state.javaState.get().msgsQueue)
      elem <- Stream.emit(queue.take())
      _ <- Stream.eval(processMsg(elem))
    } yield ()).repeat
  }

  def apply[F[_]: Concurrent: Timer: Logger](userStateRef: Ref[F, UserState[F]],
                                             privateConfService: PrivateConferenceService[F],
                                             userStateService: UserStateService[F],
                                             client: Client[F]): F[UIProgram[F]] =
    for {
      dialogAreaMVar <- MVar.empty[F, TextArea]
      jDialogMVar <- MVar.empty[F, JDialog]
    } yield new Live(userStateRef, privateConfService, userStateService, client, dialogAreaMVar, jDialogMVar)
}
