package org.encryfoundation.tg.pipelines.groupVerification

import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi.SecretChat
import org.encryfoundation.mitmImun.Prover
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base64

trait ProverStep[F[_]] {
  def prover: Prover
  def community: PrivateCommunity
  def recipientLogin: String
  def userState: Ref[F, UserState[F]]
  def client: Client[F]
  def secretChat: SecretChat
  def chatId: Long
}
