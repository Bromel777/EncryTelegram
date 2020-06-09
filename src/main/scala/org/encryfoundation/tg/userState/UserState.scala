package org.encryfoundation.tg.userState

import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.pipelines.Pipeline

import scala.collection.SortedMap
import java.util.concurrent.atomic.AtomicReference

import cats.effect.concurrent.MVar
import org.drinkless.tdlib.{Client, Client123, TdApi}
import org.javaFX.model.JUserState

case class UserState[F[_]](chatList: List[TdApi.Chat] = List.empty,
                           mainChatList: SortedMap[Long, TdApi.Chat] = SortedMap.empty[Long, TdApi.Chat],
                           chatIds: Map[Long, TdApi.Chat] = Map.empty,
                           privateGroups: Map[Long, (TdApi.Chat, String)] = Map.empty,
                           pipelineSecretChats: Map[Long, Pipeline[F]] = Map.empty[Long, Pipeline[F]],
                           pendingSecretChatsForInvite: Map[Long, (TdApi.Chat, String, TdApi.User)] = Map.empty,
                           users: Map[Int, TdApi.User] = Map.empty,
                           basicGroups: Map[Int, TdApi.BasicGroup] = Map.empty,
                           superGroups: Map[Int, TdApi.Supergroup] = Map.empty,
                           secretChats: Map[Int, TdApi.SecretChat] = Map.empty,
                           isAuth: Boolean = false,
                           client: Client[F],
                           activeChat: Long = 0,
                           javaState: AtomicReference[JUserState])
