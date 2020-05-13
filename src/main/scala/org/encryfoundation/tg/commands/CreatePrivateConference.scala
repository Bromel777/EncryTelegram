package org.encryfoundation.tg.commands

import cats.effect.Sync
import cats.effect.concurrent.Ref
import org.drinkless.tdlib.Client
import org.encryfoundation.tg.leveldb.Database
import org.encryfoundation.tg.userState.UserState
import cats.implicits._
import it.unisa.dia.gas.jpbc.{Element, Pairing}
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.sectionSeven.SectionSeven

case class CreatePrivateConference[F[_]: Sync](client: Client[F],
                                               userStateRef: Ref[F, UserState[F]],
                                               db: Database[F]) extends Command[F]{

  val pairing: Pairing = PairingFactory.getPairing("src/main/resources/properties/a.properties")

  val generatorG1: Element = pairing.getG1.newRandomElement().getImmutable

  val generatorG2: Element = generatorG1.getImmutable

  val generatorZr: Element = pairing.getZr.newRandomElement().getImmutable

  val sevenSection: SectionSeven = SectionSeven(generatorG1, generatorG2, pairing)

  override val name: String = "createConference"

  override def run(args: List[String]): F[Unit] = for {
    groupInfo <- Sync[F].delay(sevenSection.genElems(1))
    _ <- db.put(s"conf".getBytes(), args.head.getBytes())
    _ <- Sync[F].delay(println(s"Group ${args.head} created!"))
    _ <- db.put(s"conf${args.head}MySecreteKsi".getBytes(), groupInfo._1.head.userKsi.toBytes)
    _ <- db.put(s"conf${args.head}MySecreteT".getBytes(), groupInfo._1.head.userKsi.toBytes)
  } yield ()
}
