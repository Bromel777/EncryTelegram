package org.encryfoundation.tg.commands

import cats.effect.Sync
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.Client
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import it.unisa.dia.gas.jpbc.{Element, Pairing}
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.sectionSeven.SectionSeven
import org.encryfoundation.tg.community.{CommunityUser, PrivateCommunity}
import org.encryfoundation.tg.community.CommunityOps.ops._
import org.encryfoundation.tg.services.PrivateConferenceService

case class CreatePrivateConference[F[_]: Sync: Logger](userStateRef: Ref[F, UserState[F]],
                                                       db: Database[F])(
                                                       privateConfService: PrivateConferenceService[F]) extends Command[F]{

  val pairing: Pairing = PairingFactory.getPairing("properties/a.properties")

  val generatorG1: Element = pairing.getG1.newRandomElement().getImmutable

  val generatorG2: Element = generatorG1.getImmutable

  val generatorZr: Element = pairing.getZr.newRandomElement().getImmutable

  val sevenSection: SectionSeven = SectionSeven(generatorG1, generatorG2, pairing)

  override val name: String = "createConference"

  override def run(args: List[String]): F[Unit] = for {
    _ <- privateConfService.createConference(args.head, args.tail)
  } yield ()
}
