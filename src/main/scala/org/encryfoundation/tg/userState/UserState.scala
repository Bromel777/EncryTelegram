package org.encryfoundation.tg.userState

import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.pipelines.Pipeline

import scala.collection.SortedMap
import java.util.concurrent.atomic.AtomicReference

import cats.data.{Nested, OptionT}
import cats.{Applicative, FlatMap}
import cats.effect.Sync
import cats.effect.concurrent.MVar
import org.drinkless.tdlib.{Client, Client123, TdApi}
import org.encryfoundation.tg.leveldb.Database
import org.javaFX.model.JUserState
import scorex.crypto.hash.Blake2b256
import cats.Applicative
import cats.implicits._
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.services.PrivateConferenceService
import org.encryfoundation.tg.services.PrivateConferenceService._
import collection.JavaConverters._

case class UserState[F[_]: Sync](chatList: List[TdApi.Chat] = List.empty,
                                 mainChatList: SortedMap[Long, TdApi.Chat] = SortedMap.empty[Long, TdApi.Chat],
                                 chatIds: Map[Long, TdApi.Chat] = Map.empty,
                                 privateGroups: Set[PrivateGroupChat] = Set.empty,
                                 pipelineSecretChats: Map[Long, Pipeline[F]] = Map.empty[Long, Pipeline[F]],
                                 pendingSecretChatsForInvite: Map[Int, (TdApi.Chat, Pipeline[F])] = Map.empty[Int, (TdApi.Chat, Pipeline[F])],
                                 users: Map[Int, TdApi.User] = Map.empty,
                                 basicGroups: Map[Int, TdApi.BasicGroup] = Map.empty,
                                 superGroups: Map[Int, TdApi.Supergroup] = Map.empty,
                                 secretChats: Map[Int, TdApi.SecretChat] = Map.empty,
                                 privateCommunities: List[PrivateCommunity] = List.empty,
                                 isAuth: Boolean = false,
                                 client: Client[F],
                                 activeChat: Long = 0,
                                 javaState: AtomicReference[JUserState],
                                 db: Database[F])

object UserState {

  def privateGroupChatKey(chatId: Long): Array[Byte] = Blake2b256.hash(chatId + "privateChat")
  val privateChatsKey = Blake2b256.hash("privatechats")

  private def recoverState[F[_]: Sync](client: Client[F],
                                       javaState: AtomicReference[JUserState],
                                       db: Database[F]): F[Option[UserState[F]]] = {for {
    privateConfsNamesBytes <- OptionT(db.get(PrivateConferenceService.conferencesKey))
    confsNames <- OptionT.fromOption[F](ConferencesNames.parseBytes(privateConfsNamesBytes).toOption)
    privateConfs <- OptionT.liftF(recoverCommunityConfByName(confsNames.conferences, db))
    privateGroupChats <- OptionT.liftF(recoverPrivateGroupChats(db))
  } yield {
    javaState.get().communities = privateConfs.map(_.name).asJava
    UserState[F](
      client = client,
      javaState = javaState,
      db = db,
      privateCommunities = privateConfs,
      privateGroups = privateGroupChats.toSet
    )
  }}.value

  private def recoverPrivateGroupChats[F[_]: Sync](db: Database[F]): F[List[PrivateGroupChat]] =
    db.get(privateChatsKey).map {
      case Some(bytes) => PrivateGroupChats.parseBytes(bytes).get
      case None => List.empty[PrivateGroupChat]
    }

  private def recoverCommunityConfByName[F[_]: Sync](names: List[String],
                                                     db: Database[F]): F[List[PrivateCommunity]] =
    names.traverseFilter(
      name => OptionT[F, Array[Byte]](db.get(confInfo(name))).flatMap(
        bytes => OptionT.fromOption[F](PrivateCommunity.parseBytes(bytes).toOption)
      ).value
    )

  def recoverOrCreate[F[_]: Sync](client: Client[F],
                                  javaState: AtomicReference[JUserState],
                                  db: Database[F]): F[UserState[F]] =
    OptionT(recoverState(client, javaState, db)).getOrElse(
      UserState(
        client = client,
        javaState = javaState,
        db = db
      )
    )
}