package org.encryfoundation.tg.commands

import cats.effect.concurrent.Ref
import org.drinkless.tdlib.Client
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState

//class SendInvitesToPrivateGroupChat[F[_]](client: Client[F],
//                                          userStateRef: Ref[F, UserState[F]],
//                                          db: Database[F]) extends Command[F] {
//
//  override val name: String = "sendInvite"
//
//  override def run(args: List[String]): F[Unit] = for {
//
//  } yield ()
//}
