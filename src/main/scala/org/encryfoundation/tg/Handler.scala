package org.encryfoundation.tg

import java.math.BigInteger

import cats.Applicative
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.drinkless.tdlib.TdApi.MessageText
import org.drinkless.tdlib.{Client, ResultHandler, TdApi}
import org.encryfoundation.mitmImun.{Prover, Verifier}
import org.encryfoundation.tg.RunApp.sendMessage
import org.encryfoundation.tg.community.InviteStatus.{AwaitingFirstStep, CompleteFirstStep, ProverFirstStep, ProverSecondStep, ProverThirdStep, VerifierSecondStep, VerifierThirdStep}
import org.encryfoundation.tg.community.PrivateCommunityStatus.UserCommunityStatus.AwaitingSecondPhaseFromUser
import org.encryfoundation.tg.services.PrivateConferenceService
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base64

import scala.io.StdIn

case class Handler[F[_]: ConcurrentEffect: Logger](userStateRef: Ref[F, UserState[F]],
                                                   privateConferenceService: PrivateConferenceService[F],
                                                   client: Client[F]) extends ResultHandler[F] {

  /**
   * Callback called on result of query to TDLib or incoming update from TDLib.
   *
   * @param obj Result of query or update of type TdApi.Update about new events.
   */

  override def onResult(obj: TdApi.Object): F[Unit] = {
    obj.getConstructor match {
      case TdApi.UpdateAuthorizationState.CONSTRUCTOR =>
        for {
          state <- userStateRef.get
          _ <- authHandler(obj.asInstanceOf[TdApi.UpdateAuthorizationState], state.client)
        } yield ()
      case TdApi.UpdateNewChat.CONSTRUCTOR =>
        val updateNewChat: TdApi.UpdateNewChat = obj.asInstanceOf[TdApi.UpdateNewChat]
        val chat: TdApi.Chat = updateNewChat.chat
        val order = chat.order
        chat.order = 0
        for {
          state <- userStateRef.get
          _ <- userStateRef.update(_.copy(chatIds = state.chatIds + (chat.id -> chat)))
          _ <- setChatOrder(chat, order)
        } yield ()
      case TdApi.UpdateUser.CONSTRUCTOR =>
        val updateUser = obj.asInstanceOf[TdApi.UpdateUser]
        for {
          state <- userStateRef.get
          _ <- userStateRef.update(_.copy(users = state.users + (updateUser.user.id -> updateUser.user)))
        } yield ()
      case TdApi.UpdateChatOrder.CONSTRUCTOR =>
        val updateChatOrder = obj.asInstanceOf[TdApi.UpdateChatOrder]
        for {
          state <- userStateRef.get
          _ <- state.chatIds.find(_._1 == updateChatOrder.chatId).traverse { case (_, chat) =>
            setChatOrder(chat, updateChatOrder.order)
          }
        } yield ()
      case TdApi.UpdateChatLastMessage.CONSTRUCTOR =>
        val updateChat = obj.asInstanceOf[TdApi.UpdateChatLastMessage]
        for {
          state <- userStateRef.get
          _ <- state.chatIds.find(_._1 == updateChat.chatId).traverse { case (_, chat) =>
            setChatOrder(chat, updateChat.order)
          }
        } yield ()
      case TdApi.UpdateBasicGroup.CONSTRUCTOR =>
        val basicGroup = obj.asInstanceOf[TdApi.UpdateBasicGroup]
        for {
          state <- userStateRef.get
          _ <- userStateRef.update(
            _.copy(basicGroups = state.basicGroups + (basicGroup.basicGroup.id -> basicGroup.basicGroup))
          )
        } yield ()
      case TdApi.UpdateSupergroup.CONSTRUCTOR =>
        val superGroup = obj.asInstanceOf[TdApi.UpdateSupergroup]
        for {
          state <- userStateRef.get
          _ <- userStateRef.update(
            _.copy(superGroups = state.superGroups + (superGroup.supergroup.id -> superGroup.supergroup))
          )
        } yield ()
      case TdApi.UpdateSecretChat.CONSTRUCTOR =>
        val secretChat = obj.asInstanceOf[TdApi.UpdateSecretChat]
        for {
          state <- userStateRef.get
          _ <- userStateRef.update(
            _.copy(secretChats = state.secretChats + (secretChat.secretChat.id -> secretChat.secretChat))
          )
          _ <- if (
            secretChat.secretChat.state.isInstanceOf[TdApi.SecretChatStateReady] &&
              state.pendingSecretChatsForInvite.contains(secretChat.secretChat.id)
          ) for {
              _ <- Logger[F].info("Secret chat for key sharing accepted. Start first step!")
              chatInfo <- state.pendingSecretChatsForInvite(secretChat.secretChat.id).pure[F]
              groupInfo <- privateConferenceService.findConf(chatInfo._2)
              pairing <- Sync[F].delay(PairingFactory.getPairing("src/main/resources/properties/a.properties"))
              prover <- Prover(
                groupInfo.G1Gen.getImmutable,
                groupInfo.G2Gen.getImmutable,
                groupInfo.users.head.userData.userKsi.getImmutable,
                groupInfo.users.head.userData.userT.getImmutable,
                groupInfo.users.head.userData.publicKey1.getImmutable,
                groupInfo.users.head.userData.publicKey2.getImmutable,
                groupInfo.ZrGen.getImmutable,
                pairing
              ).pure[F]
              _ <- Logger[F].info("Prover data: \n" +
                s"groupInfo.G1Gen.getImmutable: ${groupInfo.G1Gen.getImmutable}\n " +
                s"groupInfo.G2Gen.getImmutable: ${groupInfo.G2Gen.getImmutable}\n " +
                s"groupInfo.users.head.userData.userKsi.getImmutable: ${groupInfo.users.head.userData.userKsi.getImmutable}\n " +
                s"groupInfo.users.head.userData.userT.getImmutable: ${groupInfo.users.head.userData.userT.getImmutable}\n " +
                s"groupInfo.users.head.userData.publicKey1.getImmutable: ${groupInfo.users.head.userData.publicKey1.getImmutable}\n " +
                s"groupInfo.users.head.userData.publicKey2.getImmutable: ${groupInfo.users.head.userData.publicKey2.getImmutable}\n " +
                s"groupInfo.ZrGen.getImmutable: ${groupInfo.ZrGen.getImmutable}")
              firstStep <- prover.firstStep().toBytes.pure[F]
              _ <- sendMessage(
                chatInfo._1.id,
                "=====First step=====",
                client
              )
              _ <- sendMessage(
                chatInfo._1.id,
                Base64.encode(firstStep),
                client
              )
              _ <- sendMessage(
                chatInfo._1.id,
                Base64.encode(groupInfo.gTilda.toBytes),
                client
              )
              _ <- sendMessage(
                chatInfo._1.id,
                Base64.encode(groupInfo.users.head.userData.publicKey1.toBytes),
                client
              )
              _ <- sendMessage(
                chatInfo._1.id,
                Base64.encode(groupInfo.users.head.userData.publicKey2.toBytes),
                client
              )
              _ <- sendMessage(
                chatInfo._1.id,
                Base64.encode(groupInfo.G1Gen.toBytes),
                client
              )
              _ <- sendMessage(
                chatInfo._1.id,
                Base64.encode(groupInfo.G2Gen.toBytes),
                client
              )
              _ <- sendMessage(
                chatInfo._1.id,
                Base64.encode(groupInfo.ZrGen.toBytes),
                client
              )
              _ <- sendMessage(
                chatInfo._1.id,
                "=====First step End=====",
                client
              )
              confUpdated <- Sync[F].delay {
                val preConfInfo = state.privateGroups(chatInfo._3)
                val previousCommunityStatus = preConfInfo._3
                val communityStatus = previousCommunityStatus.copy(
                  usersStatus = previousCommunityStatus.usersStatus + (chatInfo._2 -> AwaitingSecondPhaseFromUser)
                )
                preConfInfo.copy(_3 = communityStatus)
              }
              _ <- userStateRef.update(
                _.copy(
                  pendingSecretChatsForInvite = state.pendingSecretChatsForInvite - secretChat.secretChat.id,
                  privateGroups = state.privateGroups + (chatInfo._3 -> confUpdated),
                  inviteChats = state.inviteChats + (chatInfo._1.id -> ProverFirstStep(prover, firstStep))
                )
              )
            } yield ()
          else if (secretChat.secretChat.state.isInstanceOf[TdApi.SecretChatStatePending] && !secretChat.secretChat.isOutbound) {
            for {
              _ <- client.send(new TdApi.OpenChat(secretChat.secretChat.id), SecretChatHandler[F](userStateRef))
              state <- userStateRef.get
              _ <- userStateRef.update(
                _.copy(
                  secretChats = state.secretChats + (secretChat.secretChat.id -> secretChat.secretChat)
                )
              )
            } yield ()
          }
          else Sync[F].delay()
        } yield ()
      case TdApi.UpdateNewMessage.CONSTRUCTOR =>
        val msg = obj.asInstanceOf[TdApi.UpdateNewMessage]
        for {
          state <- userStateRef.get
          _ <- Logger[F].info(s"Receive: ${msg}")

          _ <- msg.message.content match {
            case a: MessageText if (a.text.text == "=====First step=====") && !msg.message.isOutgoing =>
              for {
                _ <- Logger[F].info("Someone trying to invite us to group!")
                _ <- userStateRef.update(_.copy(
                  inviteChats = state.inviteChats + (msg.message.chatId -> AwaitingFirstStep())
                ))
              } yield ()
            case a: MessageText if (a.text.text == "=====First step End=====") && !msg.message.isOutgoing =>
              for {
                status <- state.inviteChats(msg.message.chatId).asInstanceOf[CompleteFirstStep].pure[F]
                _ <- Logger[F].info("Init verifier!")
                pairing <- Sync[F].delay(PairingFactory.getPairing("src/main/resources/properties/a.properties"))
                genG1 <- Sync[F].delay(pairing.getG1.newElementFromBytes(status.g1GenBytes).getImmutable)
                genG2 <- Sync[F].delay(pairing.getG1.newElementFromBytes(status.g1GenBytes).getImmutable)
                genGT <- Sync[F].delay(pairing.pairing(genG1, genG2))
                genZr <- Sync[F].delay(pairing.getZr.newElementFromBytes(status.zRGenBytes).getImmutable)
                verifier <- Verifier(
                  pairing.getG1.newElementFromBytes(status.g1GenBytes).getImmutable,
                  pairing.getG2.newElementFromBytes(status.g2GenBytes).getImmutable,
                  pairing.getZr.newElementFromBytes(status.zRGenBytes).getImmutable,
                  genGT.getField.newElementFromBytes(status.firstPublicKeyBytes).getImmutable,
                  genGT.getField.newElementFromBytes(status.secondPublicKeyBytes).getImmutable,
                  genGT.getField.newElementFromBytes(status.gTildaBytes).getImmutable,
                  pairing.getZr.newRandomElement(),
                  pairing
                ).pure[F]
                secondStep <- verifier.secondStep().toBytes.pure[F]
                _ <- userStateRef.update(_.copy(
                  inviteChats = state.inviteChats + (
                      msg.message.chatId -> VerifierSecondStep(verifier, status.firstStepBytes, secondStep)
                    )
                ))
                _ <- sendMessage(
                  msg.message.chatId,
                  "=====Second step=====",
                  client
                )
                _ <- sendMessage(
                  msg.message.chatId,
                  Base64.encode(secondStep),
                  client
                )
                _ <- Logger[F].info(s"My public key: ${Base64.encode(verifier.publicKey.toBytes)}")
                _ <- sendMessage(
                  msg.message.chatId,
                  Base64.encode(verifier.publicKey.toBytes),
                  client
                )
                _ <- sendMessage(
                  msg.message.chatId,
                  "=====Second step End=====",
                  client
                )
              } yield ()
            case a: MessageText if (a.text.text == "=====Second step=====") && !msg.message.isOutgoing =>
              for {
                prevStatus <- state.inviteChats(msg.message.chatId).pure[F]
                _ <- Logger[F].info("Wow! Recipient accept our invite.")
                _ <- userStateRef.update(_.copy(
                  inviteChats = state.inviteChats +
                    (msg.message.chatId -> prevStatus.asInstanceOf[ProverFirstStep].copy(canProcess = true))
                ))
              } yield ()
            case a: MessageText if (a.text.text == "=====Second step End=====") && !msg.message.isOutgoing =>
              for {
                status <- state.inviteChats(msg.message.chatId).asInstanceOf[ProverThirdStep].pure[F]
                pairing <- Sync[F].delay(PairingFactory.getPairing("src/main/resources/properties/a.properties"))
                thirdStep <- status.prover.thirdStep(
                  pairing.getG1.newElementFromBytes(status.secondStepByte).getImmutable
                ).pure[F]
                _ <- sendMessage(
                  msg.message.chatId,
                  "=====Third step=====",
                  client
                )
                _ <- sendMessage(
                  msg.message.chatId,
                  Base64.encode(thirdStep),
                  client
                )
                _ <- sendMessage(
                  msg.message.chatId,
                  "=====Third step End=====",
                  client
                )
                _ <- userStateRef.update(_.copy(
                  inviteChats = state.inviteChats + (msg.message.chatId -> AwaitingFirstStep())
                ))
                _ <- Logger[F].info(s"Verifier pubKey: ${Base64.encode(status.verifierPubKey)}")
                _ <- Logger[F].info(s"Ver pubkey (1): ${status.prover.generator3.getField.newElementFromBytes(status.verifierPubKey)}")
                _ <- Logger[F].info(s"Ver pubkey (2): ${pairing.getGT.newElementFromBytes(status.verifierPubKey)}")
                _ <- Logger[F].info(s"my pub key1: ${Base64.encode(status.prover.publicKey1.toBytes)}")
                _ <- Logger[F].info(s"my pub key2: ${Base64.encode(status.prover.publicKey2.toBytes)}")
                _ <- Logger[F].info(s"First step: ${Base64.encode(status.firstStep)}")
                _ <- Logger[F].info(s"First step: ${pairing.getG1.newElementFromBytes(status.firstStep).getImmutable}")
                _ <- Logger[F].info(s"Second step: ${Base64.encode(status.secondStepByte)}")
                _ <- Logger[F].info(s"Second step: ${pairing.getG1.newElementFromBytes(status.secondStepByte).getImmutable}")
                commonKey <- Sync[F].delay(
                  status.prover.produceCommonKey(
                    status.prover.generator3.getField.newElementFromBytes(status.verifierPubKey).getImmutable,
                    pairing.getG1.newElementFromBytes(status.firstStep).getImmutable,
                    pairing.getG1.newElementFromBytes(status.secondStepByte).getImmutable
                  )
                )
                _ <- Logger[F].info(s"Common key: ${Base64.encode(commonKey)}")
                _ <- client.send(
                  new TdApi.CloseChat(msg.message.id),
                  EmptyHandler[F]()
                )
              } yield ()
            case a: MessageText if (a.text.text == "=====Third step=====") && !msg.message.isOutgoing =>
              for {
                prevStatus <- state.inviteChats(msg.message.chatId).pure[F]
                _ <- Logger[F].info("Ok. Catch last elem.")
                _ <- userStateRef.update(_.copy(
                  inviteChats = state.inviteChats +
                    (msg.message.chatId -> prevStatus.asInstanceOf[VerifierSecondStep].copy(canProcess = true))
                ))
              } yield ()
            case a: MessageText if (a.text.text == "=====Third step End=====") && !msg.message.isOutgoing =>
              for {
                status <- state.inviteChats(msg.message.chatId).asInstanceOf[VerifierThirdStep].pure[F]
                pairing <- Sync[F].delay(PairingFactory.getPairing("src/main/resources/properties/a.properties"))
                _ <- Logger[F].info(s"Ok. Verify result is: ${status.verifier.forthStep(
                    pairing.getG1.newElementFromBytes(status.firstStepBytes).getImmutable,
                    pairing.getG1.newElementFromBytes(status.secondStepBytes).getImmutable,
                    status.thirdStepBytes
                )}.\n ")
                _ <- Logger[F].info(s"First step: ${Base64.encode(status.firstStepBytes)}")
                _ <- Logger[F].info(s"Second step: ${Base64.encode(status.secondStepBytes)}")
                _ <- Logger[F].info(s"pubKey1: ${Base64.encode(status.verifier.RoI1.toBytes)}")
                _ <- Logger[F].info(s"pubKey2: ${Base64.encode(status.verifier.RoI2.toBytes)}")
                _ <- Logger[F].info(s"my pubKey: ${Base64.encode(status.verifier.publicKey.toBytes)}")
                commonKey <- Sync[F].delay(
                  status.verifier.produceCommonKey(
                    pairing.getG1.newElementFromBytes(status.firstStepBytes).getImmutable,
                    pairing.getG1.newElementFromBytes(status.secondStepBytes).getImmutable,
                    status.verifier.RoI1.getImmutable,
                    status.verifier.RoI2.getImmutable
                  )
                )
                _ <- Logger[F].info(s"Common key: ${Base64.encode(commonKey)}")
                _ <- client.send(
                  new TdApi.CloseChat(msg.message.chatId),
                  EmptyHandler[F]()
                )
              } yield ()
            case a: MessageText if (a.text.text == "=====Second step=====") =>
              Applicative[F].pure(())
            case a: MessageText if (a.text.text == "=====Second step End=====") =>
              Applicative[F].pure(())
            case a: MessageText if (a.text.text == "=====First step=====") =>
              Applicative[F].pure(())
            case a: MessageText if (a.text.text == "=====First step End=====") =>
              Applicative[F].pure(())
            case a: MessageText if (a.text.text == "=====Third step=====") =>
              Applicative[F].pure(())
            case a: MessageText if (a.text.text == "=====Third step End=====") =>
              Applicative[F].pure(())
            case a: MessageText if state.inviteChats.contains(msg.message.chatId) =>
              for {
                prevStatus <- state.inviteChats(msg.message.chatId).pure[F]
                _ <- Logger[F].info(s"Update status for chat ${msg.message.chatId}. Prev status: ${prevStatus}")
                _ <- userStateRef.update(_.copy(
                  inviteChats = state.inviteChats + (
                      msg.message.chatId -> prevStatus.setStepValue(Base64.decode(a.text.text).get)
                    )
                ))
              } yield ()
            case _ => (()).pure[F]
          }
        } yield ()
      case any => Logger[F].info(s"Receive unkown elem: ${obj}")
    }
  }

  def setChatOrder(chat: TdApi.Chat, order: Long): F[Unit] = {
    for {
      state <- userStateRef.get
      //_ <- Sync[F].delay(println(s"Get chat order: ${order}"))
      _ <- if (chat.order != 0)
            userStateRef.update(_.copy(mainChatList = state.mainChatList.filter(_.order == chat.order)))
          else (()).pure[F]
      _ <- Sync[F].delay(chat.order = order)
      _ <- if (order != 0)
            userStateRef.update(
              _.copy(
                mainChatList = (chat :: state.mainChatList).sortBy(_.order).takeRight(20).reverse
              )
            )
          else (()).pure[F]
    } yield ()
  }

  def authHandler(authEvent: TdApi.UpdateAuthorizationState, client: Client[F]): F[Unit] = {
    authEvent.authorizationState match {
      case a: TdApi.AuthorizationStateWaitTdlibParameters =>
        val parameters = new TdApi.TdlibParameters
        parameters.databaseDirectory = "tdlib"
        parameters.useMessageDatabase = true
        parameters.useSecretChats = true
        parameters.apiId = 1257765
        parameters.apiHash = "8f6d710676dd9cb77c6c7fe24f09ee15"
        parameters.systemLanguageCode = "en"
        parameters.deviceModel = "Desktop"
        parameters.systemVersion = "Unknown"
        parameters.applicationVersion = "0.1"
        parameters.enableStorageOptimizer = true
        Logger[F].info("Setting td-lib settings") >> client.send(
          new TdApi.SetTdlibParameters(parameters), AuthRequestHandler[F]()
        )
      case a: TdApi.AuthorizationStateWaitEncryptionKey =>
        client.send(new TdApi.CheckDatabaseEncryptionKey(), AuthRequestHandler[F]())
      case a: TdApi.AuthorizationStateWaitPhoneNumber =>
        println("Enter phone number:")
        val phoneNumber = StdIn.readLine()
        client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), AuthRequestHandler())
      case a: TdApi.AuthorizationStateWaitCode =>
        println("Enter code number:")
        val code = StdIn.readLine()
        client.send(new TdApi.CheckAuthenticationCode(code), AuthRequestHandler())
      case a: TdApi.AuthorizationStateWaitPassword =>
        println("Enter password")
        val pass = StdIn.readLine()
        client.send(new TdApi.CheckAuthenticationPassword(pass), AuthRequestHandler())
      case a: TdApi.AuthorizationStateReady =>
        userStateRef.update(_.copy(isAuth = true)).map(_ => ())
      case _ =>
        println(s"Got unknown event in auth. ${authEvent}").pure[F]
    }
  }
}

object Handler {
  def apply[F[_]: ConcurrentEffect: Logger](stateRef: Ref[F, UserState[F]],
                                            queueRef: Ref[F, List[TdApi.Object]],
                                            privateConferenceService: PrivateConferenceService[F],
                                            client: Client[F]): F[Handler[F]] = {
    val handler = new Handler(stateRef, privateConferenceService, client)
    for {
      list <- queueRef.get
      _ <- Sync[F].delay(list)
      _ <- list.foreach(elem => Sync[F].delay(println(s"Send: ${elem.getConstructor}. ${list}")) >> handler.onResult(elem)).pure[F]
      handlerExp <- handler.pure[F]
    } yield handlerExp
  }
}
