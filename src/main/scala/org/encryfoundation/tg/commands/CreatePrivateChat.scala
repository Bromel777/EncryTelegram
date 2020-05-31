package org.encryfoundation.tg.commands

import cats.effect.{Concurrent, Sync}
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.{Client, TdApi}
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.tg.{EmptyHandler, SecretChatHandler}

case class CreatePrivateChat[F[_]: Concurrent: Logger](client: Client[F],
                                                       userStateRef: Ref[F, UserState[F]]) extends Command[F] {

  override val name: String = "createPrivateChat"

  override def run(args: List[String]): F[Unit] = for {
    userLogin <- args.head.pure[F]
    state <- userStateRef.get
    userId <- state.users.find(info => info._2.username == userLogin || info._2.phoneNumber == args.mkString(" ")).get._2.id.pure[F]
    _ <- client.send(new TdApi.CreateNewSecretChat(userId), SecretChatHandler[F](userStateRef))
  } yield ()
}
