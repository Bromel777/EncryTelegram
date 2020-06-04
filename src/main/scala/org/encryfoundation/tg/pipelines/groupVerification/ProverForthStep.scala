package org.encryfoundation.tg.pipelines.groupVerification

import cats.effect.concurrent.Ref
import it.unisa.dia.gas.jpbc.Element
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.mitmImun.Prover
import org.encryfoundation.tg.community.PrivateCommunity
import org.encryfoundation.tg.pipelines.Pipeline
import org.encryfoundation.tg.userState.UserState

case class ProverForthStep[F[_]](prover: Prover,
                                 community: PrivateCommunity,
                                 recipientLogin: String,
                                 userState: Ref[F, UserState[F]],
                                 client: Client[F],
                                 secretChat: TdApi.Chat,
                                 chatId: Long,
                                 firstStep: Element,
                                 secondStep: Element,
                                 thirdStep: Array[Byte]) extends Pipeline[F] {

  override def processInput(input: Array[Byte]): F[Pipeline[F]] = ???
}
