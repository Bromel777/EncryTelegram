package org.encryfoundation.tg.pipelines.groupVerification

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import it.unisa.dia.gas.jpbc.Element
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi.SecretChat
import org.encryfoundation.mitmImun.Prover
import org.encryfoundation.tg.RunApp.sendMessage
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.userState.UserState
import scorex.crypto.encode.Base64

case class ProverSecondStep[F[_]: Concurrent: Timer](prover: Prover,
                                                     community: PrivateCommunity,
                                                     recipientLogin: String,
                                                     userState: Ref[F, UserState[F]],
                                                     client: Client[F],
                                                     secretChat: SecretChat,
                                                     chatId: Long,
                                                     firstStep: Element) extends Pipeline[F] {

  private def send2Chat(msg: Array[Byte]): F[Unit] =
    sendMessage(
      chatId,
      Base64.encode(msg),
      client
    )

  override def processInput(input: Array[Byte]): F[Pipeline[F]] = ???
}
