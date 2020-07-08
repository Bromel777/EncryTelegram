package org.encryfoundation.tg.userState

import java.util.concurrent.atomic.AtomicReference

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.services.PrivateConferenceService
import org.encryfoundation.tg.services.PrivateConferenceService._
import org.encryfoundation.tg.steps.Step
import org.encryfoundation.tg.steps.Step.InitStep
import org.javaFX.model.{JLocalCommunity, JUserState}
import scorex.crypto.hash.Blake2b256

import scala.collection.JavaConverters._
import scala.collection.SortedMap

case class UserState[F[_]: Sync](chatList: List[TdApi.Chat] = List.empty,
                                 mainChatList: SortedMap[Long, TdApi.Chat] = SortedMap.empty[Long, TdApi.Chat],
                                 chatIds: Map[Long, TdApi.Chat] = Map.empty,
                                 privateGroups: Set[PrivateGroupChat] = Set.empty,
                                 pipelineSecretChats: Map[Long, Pipeline[F]] = Map.empty[Long, Pipeline[F]],
                                 pendingSecretChatsForInvite: Map[Long, (TdApi.Chat, Pipeline[F])] = Map.empty[Long, (TdApi.Chat, Pipeline[F])],
                                 pipelineSecretChatInfo: Map[Int, Long] = Map.empty[Int, Long],
                                 users: Map[Int, TdApi.User] = Map.empty,
                                 basicGroups: Map[Int, TdApi.BasicGroup] = Map.empty,
                                 superGroups: Map[Int, TdApi.Supergroup] = Map.empty,
                                 secretChats: Map[Int, TdApi.SecretChat] = Map.empty,
                                 privateCommunities: List[PrivateCommunity] = List.empty,
                                 isAuth: Boolean = false,
                                 client: Client[F],
                                 activeChat: Long = 0,
                                 currentStep: Step = InitStep,
                                 javaState: AtomicReference[JUserState],
                                 db: Database[F])

object UserState {

  def privateGroupChatKey(chatId: Long): Array[Byte] = Blake2b256.hash(chatId + "privateChat")
  val privateChatsKey = Blake2b256.hash("privatechats")

  private def recoverState[F[_]: Sync](client: Client[F],
                                       javaState: AtomicReference[JUserState],
                                       db: Database[F]): F[UserState[F]] = for {
    privateConfs <- recoverCommunities(db)
    privateGroupChats <- recoverPrivateGroupChats(db)
  } yield {
    privateConfs.foreach(community =>
      javaState.get().communities.add(new JLocalCommunity(community.name, community.users.length))
    )
    UserState[F](
      client = client,
      javaState = javaState,
      db = db,
      privateCommunities = privateConfs,
      privateGroups = privateGroupChats.toSet
    )
  }

  private def recoverPrivateGroupChats[F[_]: Sync](db: Database[F]): F[List[PrivateGroupChat]] =
    db.get(privateChatsKey).map {
      case Some(bytes) => PrivateGroupChats.parseBytes(bytes).get
      case None => List.empty[PrivateGroupChat]
    }

  private def recoverCommunities[F[_]: Sync](db: Database[F]): F[List[PrivateCommunity]] = (for {
    privateConfsNamesBytes <- OptionT(db.get(PrivateConferenceService.conferencesKey))
    confsNames <- OptionT.fromOption[F](ConferencesNames.parseBytes(privateConfsNamesBytes).toOption)
    privateConfs <- OptionT.liftF(recoverCommunityConfByName(confsNames.conferences, db))
  } yield (privateConfs)).getOrElse(List.empty)

  private def recoverCommunityConfByName[F[_]: Sync](names: List[String],
                                                     db: Database[F]): F[List[PrivateCommunity]] =
    names.traverseFilter(
      name => OptionT[F, Array[Byte]](db.get(confInfo(name))).flatMap(
        bytes => OptionT.fromOption[F](PrivateCommunity.parseBytes(bytes).toOption)
      ).value
    )

  def recoverOrCreate[F[_]: Sync](client: Client[F],
                                  javaState: AtomicReference[JUserState],
                                  db: Database[F]): F[UserState[F]] = recoverState(client, javaState, db)
}