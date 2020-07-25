package org.encryfoundation.tg.services

import java.nio.charset.StandardCharsets

import cats.Applicative
import cats.effect.Sync
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.TdApi
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.userState.{PrivateGroupChat, PrivateGroupChats, UserState}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.drinkless.tdlib.TdApi.MessageText
import org.encryfoundation.tg.crypto.AESEncryption
import org.encryfoundation.tg.handlers.EmptyHandler
import org.encryfoundation.tg.pipelines.utilPipes.EmptyPipeline
import org.encryfoundation.tg.steps.Step
import org.encryfoundation.tg.steps.Step.ChatsStep
import org.encryfoundation.tg.utils.{ChatUtils, MessagesUtils}
import scorex.crypto.encode.Base64

import collection.JavaConverters._
import scala.util.Try

trait UserStateService[F[_]] {
  //common ops
  def setAuth(): F[Unit]
  def logout(): F[Unit]
  //basic chats ops
  def addChat(chat: TdApi.Chat): F[Unit]
  def updateChatOrder(chat: TdApi.Chat, newOrder: Long): F[Unit]
  def getChatById(chatId: Long): F[Option[TdApi.Chat]]
  def updateUnreadMsgsCount(chatId: Long, newCount: Int): F[Unit]
  def increaseChatLimit(newValue: Int): F[Unit]
  //secret chats
  def addSecretChat(chat: TdApi.SecretChat): F[Unit]
  def removeSecretChat(chat: TdApi.SecretChat): F[Unit]
  //private group chats
  def getPrivateGroupChat(chatId: Long): F[Option[PrivateGroupChat]]
  def persistPrivateGroupChat(privateGroupChat: PrivateGroupChat): F[Unit]
  //groups
  def updateBasicGroup(basicGroup: TdApi.UpdateBasicGroup): F[Unit]
  def updateSuperGroup(superGroup: TdApi.UpdateSupergroup): F[Unit]
  //pipeline chats
  def addPipelineChat(chat: TdApi.Chat, newPipeline: Pipeline[F]): F[Unit]
  def updatePipelineChat(secretChatId: Long, newPipeline: Pipeline[F]): F[Unit]
  def addPendingPipelineChat(chat: TdApi.Chat, newPipeline: Pipeline[F]): F[Unit]
  def getPipelineChatIdBySecChat(secretChatId: Int): F[Option[Long]]
  def getPipeline(chatId: Long): F[Option[Pipeline[F]]]
  def getPendingPipeline(chatId: Long): F[Option[Pipeline[F]]]
  //users
  def updateUser(user: TdApi.User): F[Unit]
  def getUserById(userId: Int): F[Option[TdApi.User]]
  //communities
  def deleteCommunity(communityName: String): F[Unit]
  //steps
  def setCurrentStep(step: Step): F[Unit]
}

object UserStateService {

  private final class Live[F[_]: Sync: Logger](userState: Ref[F, UserState[F]],
                                               chatLimitRef: Ref[F, Int],
                                               db: Database[F]) extends UserStateService[F] {

    override def addChat(chat: TdApi.Chat): F[Unit] =
      userState.update { prevState =>
        if (!prevState.pendingSecretChatsForInvite.exists(_._2._1.id == chat.id) &&
          !prevState.pipelineSecretChats.contains(chat.id))
          prevState.copy(chatIds = prevState.chatIds + (chat.id -> chat))
        else prevState
      }

    override def getChatById(chatId: Long): F[Option[TdApi.Chat]] =
      userState.get.map(_.chatIds.find(_._2.id == chatId).map(_._2))

    override def addSecretChat(chat: TdApi.SecretChat): F[Unit] =
      userState.update { prevState =>
        if (!prevState.pendingSecretChatsForInvite.exists(_._2._1.id == chat.id) &&
          !prevState.pipelineSecretChats.contains(chat.id))
          prevState.copy(secretChats = prevState.secretChats + (chat.id -> chat))
        else prevState
      }

    override def removeSecretChat(chat: TdApi.SecretChat): F[Unit] =
      userState.update { prevState =>
        prevState.copy(secretChats = prevState.secretChats - chat.id)
      }

    override def persistPrivateGroupChat(privateGroupChat: PrivateGroupChat): F[Unit] =
      userState.updateAndGet(prevState =>
        prevState.copy(
          privateGroups = prevState.privateGroups + privateGroupChat
        )
      ).flatMap(newState =>
        db.put(UserState.privateChatsKey, PrivateGroupChats.toBytes(newState.privateGroups.toList))
      ) >> Logger[F].info(s"Persist private group chat: ${privateGroupChat}")

    override def addPipelineChat(chat: TdApi.Chat, pipeline: Pipeline[F]): F[Unit] =
      chatLimitRef.get.flatMap { chatLimit =>
        userState.update { prevState =>
          prevState.javaState.get().setChatList(
            prevState.chatList.filterNot(_.id == chat.id).sortBy(_.order).takeRight(chatLimit).reverse.asJava
          )
          prevState.copy(
            pipelineSecretChats = prevState.pipelineSecretChats + (chat.id -> pipeline)
          )
        }
      }

    override def updatePipelineChat(chatId: Long, newPipeline: Pipeline[F]): F[Unit] =
      newPipeline match {
        case EmptyPipeline(chatId) =>
            userState.update { prevState =>
              prevState.copy(
                pendingSecretChatsForInvite = prevState.pendingSecretChatsForInvite - chatId,
                pipelineSecretChats = prevState.pipelineSecretChats - chatId
              )
            }
        case _ =>
          for {
            state <- userState.get
            pending <- state.pendingSecretChatsForInvite.get(chatId).pure[F]
            chatLimit <- chatLimitRef.get
            _ <- if (pending.nonEmpty) userState.update { prevState =>
              prevState.javaState.get().setChatList(
                prevState.chatList.filterNot(_.id == chatId).sortBy(_.order).takeRight(chatLimit).reverse.asJava
              )
              prevState.copy(
                pendingSecretChatsForInvite = prevState.pendingSecretChatsForInvite - chatId,
                pipelineSecretChats = state.pipelineSecretChats + (pending.get._1.id -> newPipeline)
              )
            } else userState.update { prevState =>
              prevState.javaState.get().setChatList(
                prevState.chatList.filterNot(_.id == chatId).sortBy(_.order).takeRight(chatLimit).reverse.asJava
              )
              prevState.copy(
                pipelineSecretChats = state.pipelineSecretChats + (chatId -> newPipeline)
              )
            }
          } yield ()
      }

    override def getPipeline(secretChatId: Long): F[Option[Pipeline[F]]] = for {
      state <- userState.get
      _ <- Logger[F].info(s"pipelines: ${state.pendingSecretChatsForInvite}. ${state.pipelineSecretChats}")
      pendingPipeLine <- state.pendingSecretChatsForInvite.find(_._1 == secretChatId).map(_._2._2).pure[F]
      privatePipeline <- state.pipelineSecretChats.find(_._1 == secretChatId).map(_._2).pure[F]
    } yield pendingPipeLine.orElse(privatePipeline)

    override def getPendingPipeline(secretChatId: Long): F[Option[Pipeline[F]]] =
      userState.get.flatMap { state =>
        state.pendingSecretChatsForInvite.find(_._1 == secretChatId).map(_._2._2).pure[F]
      }

    override def getPrivateGroupChat(chatId: Long): F[Option[PrivateGroupChat]] =
      userState.get.map(_.privateGroups.find(_.chatId == chatId))

    override def addPendingPipelineChat(chat: TdApi.Chat,
                                        newPipeline: Pipeline[F]): F[Unit] = chat.`type` match {
      case secret: TdApi.ChatTypeSecret =>
        for {
          _ <- Logger[F].info(s"Add chat with id: ${chat.id} and secret chat id: ${secret.secretChatId}")
          chatLimit <- chatLimitRef.get
          _ <- userState.update { prevState =>
            prevState.javaState.get().setChatList(
              prevState.chatList.filterNot(_.id == chat.id).sortBy(_.order).takeRight(chatLimit).reverse.asJava
            )
            prevState.copy(
              pendingSecretChatsForInvite = prevState.pendingSecretChatsForInvite + (chat.id -> (chat, newPipeline)),
              pipelineSecretChatInfo = prevState.pipelineSecretChatInfo + (secret.secretChatId -> chat.id)
            )
          }
        } yield ()
      case _ =>  ().pure[F]
    }

    override def updateUser(user: TdApi.User): F[Unit] = for {
      _ <- userState.update { prevState =>
        val newUsers = prevState.javaState.get().getUsersMap
        newUsers.put(user.id.toLong, user)
        prevState.javaState.get().setUsersMap(newUsers)
        prevState.copy(users = prevState.users + (user.id -> user))
      }
    } yield ()

    override def updateChatOrder(chat: TdApi.Chat, newOrder: Long): F[Unit] = {

      def checkForPipelineMsg(chat: TdApi.Chat): Boolean =
        (MessagesUtils.pipelinesStartMsg ++ MessagesUtils.pipelinesEndMsg)
          .contains(MessagesUtils.tdMsg2String(chat.lastMessage))

      def isPipeline(chatForChat: TdApi.Chat, state: UserState[F]): Boolean = {
        state.pendingSecretChatsForInvite.exists(_._2._1.id == chat.id) ||
          state.pendingSecretChatsForInvite.exists(_._1 == chat.id) ||
          state.pipelineSecretChats.contains(chat.id) || checkForPipelineMsg(chatForChat)
      }

      userState.get.flatMap { state =>
        if (!state.pendingSecretChatsForInvite.exists(_._2._1.id == chat.id) &&
          !state.pipelineSecretChats.contains(chat.id))
          for {
            chatLimit <- chatLimitRef.get
            _ <- userState.update(prevState =>
              if (prevState.chatList.length < chatLimit) prevState.copy(
                chatList = prevState.mainChatList.takeRight(chatLimit).values.toList
              ) else prevState
            )
            isSecretGroupChat <- Applicative[F].pure(state.privateGroups.find(_.chatId == chat.id))
            decryptedMsg <- Applicative[F].pure(
              if (chat.lastMessage != null) isSecretGroupChat match {
              case Some(privateGroupChat) =>
                chat.lastMessage.content match {
                  case text: MessageText =>
                    val aes = AESEncryption(privateGroupChat.password.getBytes())
                    val msgText = Try(new String(aes.decrypt(Base64.decode(text.text.text).get), StandardCharsets.UTF_8)).getOrElse("Unknown msg")
                    val newText = text
                    newText.text.text = msgText
                    chat.lastMessage.content = newText
                    chat.lastMessage
                  case _ => chat.lastMessage
                }
              case None => chat.lastMessage
            } else chat.lastMessage )
            _ <- Sync[F].delay {
              chat.order = newOrder
              chat.lastMessage = decryptedMsg
            }
            _ <- userState.update(prevState =>
              if (newOrder != 0 && !isPipeline(chat, prevState)) {
                prevState.javaState.get().setChatList(
                  (chat :: prevState.chatList.filterNot(el => el.id == chat.id || isPipeline(el, prevState))).sortBy(_.order).takeRight(chatLimit).reverse.asJava
                )
                prevState.copy(
                  chatList = (chat :: prevState.chatList.filterNot(_.id == chat.id)).sortBy(_.order),
                  mainChatList = (prevState.mainChatList.filterNot(_._2.id == chat.id) + (newOrder -> chat))
                )
              } else prevState
            )
          } yield ()
        else ().pure[F]
      }
    }

    override def updateBasicGroup(basicGroup: TdApi.UpdateBasicGroup): F[Unit] =
      Logger[F].info("Invoke updateBasiGroup") >>
      userState.update(prevState =>
        prevState.copy(basicGroups = prevState.basicGroups + (basicGroup.basicGroup.id -> basicGroup.basicGroup))
      )

    override def updateSuperGroup(superGroup: TdApi.UpdateSupergroup): F[Unit] =
      userState.update(prevState =>
        prevState.copy(superGroups = prevState.superGroups + (superGroup.supergroup.id -> superGroup.supergroup))
      )

    override def getPipelineChatIdBySecChat(secretChatId: Int): F[Option[Long]] =
      Logger[F].info("Invoke getPipelineChatIdBySecChat") >>
      userState.get.map(_.pipelineSecretChatInfo.get(secretChatId))

    override def setAuth(): F[Unit] =
      userState.update { prevState =>
        prevState.javaState.get().setAuth(true)
        prevState.copy(isAuth = true)
      } >> setCurrentStep(ChatsStep)

    override def setCurrentStep(step: Step): F[Unit] =
      Logger[F].info(s"Switch current step to ${step}") >> userState.update { prevState =>
        prevState.copy(currentStep = step)
      }

    override def getUserById(userId: Int): F[Option[TdApi.User]] =
      userState.get.map(_.users.get(userId))

    override def logout(): F[Unit] = for {
      _ <- userState.update(_.copy(isAuth = false))
      keys <- db.getAllKeys()
      _ <- keys.traverse(db.remove)
      _ <- userState.get.map{ prevState =>
        prevState.javaState.get().inQueue.clear()
        prevState.javaState.get().logout()
      }
    } yield ()

    override def deleteCommunity(communityName: String): F[Unit] =
      userState.update { prevState =>
        prevState.javaState.get().communities =
          prevState.javaState.get().communities.asScala.filter(_.getCommunityName != communityName).asJava
        prevState.copy(
          privateCommunities = prevState.privateCommunities.filter(_.name == communityName)
        )
      }

    override def updateUnreadMsgsCount(chatId: Long, newCount: Int): F[Unit] =
      userState.get.map { prevState =>
        prevState.mainChatList.find(_._2.id == chatId).map(_._2.unreadCount = newCount)
      } >> ().pure[F]

    override def increaseChatLimit(newValue: Int): F[Unit] =
      chatLimitRef.set(newValue) >> userState.update { prevState =>
        prevState.javaState.get().setChatList(
          prevState.chatList.sortBy(_.order).takeRight(newValue).reverse.asJava
        )
        prevState.copy(
          chatList = prevState.mainChatList.values.toList.takeRight(newValue)
        )
      }
  }

  def apply[F[_]: Sync: Logger](db: Database[F],
                                userState: Ref[F, UserState[F]]): F[UserStateService[F]] =
    Ref.of[F, Int](20).map { chatLimit =>
      new Live[F](userState, chatLimit,  db)
    }
}
