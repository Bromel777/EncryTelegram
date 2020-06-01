package org.encryfoundation.tg.services

import java.math.BigInteger

import cats.FlatMap
import cats.effect.Sync
import it.unisa.dia.gas.jpbc.{Element, Pairing}
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.sectionSeven.SectionSeven
import org.encryfoundation.tg.leveldb.Database
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.encryfoundation.mitmImun.{Prover, Verifier}
import org.encryfoundation.tg.community.{CommunityUser, PrivateCommunity}
import scorex.crypto.encode.Base64
import scorex.crypto.hash.Blake2b256

import scala.concurrent.Future

trait PrivateConferenceService[F[_]] {
  def createConference(name: String, users: List[String]): F[Unit]
  def addUserToConf(conf: String, userName: String): F[Unit]
  def sendInvite(conf: String, userName: String): F[Unit]
  def findConf(conf: String): F[PrivateCommunity]
  def saveMyConfCredentials(conf: String, userInfo: CommunityUser): F[Unit]
  def deleteUserFromConf(conf: String, userName: String): F[Unit]
  def getConfs: F[List[PrivateCommunity]]
}

object PrivateConferenceService {

  private class Live[F[_]: Sync: Logger](db: Database[F]) extends PrivateConferenceService[F] {

    private val conferencesKey = Blake2b256("Conferences")
    private def confInfo(confName: String) = Blake2b256(s"ConfInfo${confName}")

    override def createConference(name: String, users: List[String]): F[Unit] =
      for {
        pairing <- Sync[F].delay(PairingFactory.getPairing("src/main/resources/properties/a.properties"))
        generatorG1 <- Sync[F].delay(pairing.getG1.newRandomElement().getImmutable)
        generatorG2 <- Sync[F].delay(generatorG1.getImmutable)
        generatorZr <- Sync[F].delay(pairing.getZr.newRandomElement().getImmutable)
        sevSec <- Sync[F].delay(SectionSeven(generatorG1, generatorG2, pairing))
        usersInfo <- Sync[F].delay(sevSec.genElems(users.length))
        usersIds <- users.zip(usersInfo._1).map { case (userLogin, userInfo) =>
          CommunityUser(userLogin, userInfo)
        }.pure[F]
        community <- PrivateCommunity(name, usersIds, generatorG1, generatorG2, generatorZr, usersInfo._2).pure[F]
        _ <- Sync[F].delay {
          val user = usersInfo._1.head
          val key = pairing.getZr.newElement(new BigInteger("434468738970006145264306173286722283984358389443"))
          val prover = Prover(generatorG1, generatorG2, user.userKsi, user.userT, user.publicKey1, user.publicKey2, generatorZr, pairing)
          println(s"Prover info:\n " +
            s"generatorG1: ${generatorG1}\n " +
            s"generatorG2: ${generatorG2}\n " +
            s"user.userKsi: ${user.userKsi}\n " +
            s"user.userT: ${user.userT}\n " +
            s"user.publicKey1: ${user.publicKey1}\n " +
            s"user.publicKey2: ${user.publicKey2}\n " +
            s"generatorZr: ${generatorZr}")
          val verifier = Verifier(generatorG1, generatorG2, generatorZr, user.publicKey1, user.publicKey2, usersInfo._2, key, pairing)
          val S1 = prover.firstStep()
          val S2 = verifier.secondStep()
          val c = prover.thirdStep(S2)
          val res = verifier.forthStep(S1, S2, c)
          println(s"Res: ${res}")
          println(s"verifier.publicKey: ${Base64.encode(verifier.publicKey.toBytes)}")
          println(s"verifier.publicKey: ${verifier.publicKey}")
          println(s"s1: ${Base64.encode(S1.toBytes)}")
          println(s"s1: ${S1}")
          println(s"s2: ${Base64.encode(S2.toBytes)}")
          println(s"s1: ${S2}")
          println(s"prover.publicKey1: ${Base64.encode(user.publicKey1.toBytes)}")
          println(s"prover.publicKey2: ${Base64.encode(user.publicKey2.toBytes)}")
          val key1 = prover.produceCommonKey(verifier.publicKey, S1, S2)
          val key2 = verifier.produceCommonKey(S1, S2, user.publicKey1, user.publicKey2)
          println(s"key1: ${Base64.encode(key1)}")
          println(s"Key2: ${Base64.encode(key2)}")
        }
        _ <- Logger[F].info(s"Create private community with name: ${name}. And users: ${usersIds.map(_.userTelegramLogin)}")
        _ <- db.put(conferencesKey, name.getBytes())
        _ <- db.put(confInfo(name), PrivateCommunity.toBytes(community))
      } yield ()

    override def addUserToConf(conf: String, userName: String): F[Unit] = ???

    override def deleteUserFromConf(conf: String, userName: String): F[Unit] = ???

    override def getConfs: F[List[PrivateCommunity]] = for {
      activeConference <- db.get(conferencesKey)
      conferenceInfo <- db
        .get(confInfo(activeConference.get.map(_.toChar).mkString))
        .map(confBytes => PrivateCommunity.parseBytes(confBytes.get))
    } yield List(conferenceInfo.get)

    override def saveMyConfCredentials(conf: String,
                                       userInfo: CommunityUser): F[Unit] = ???

    override def sendInvite(conf: String, userName: String): F[Unit] = ???

    override def findConf(conf: String): F[PrivateCommunity] =
      getConfs.map(_.find(_.name == conf).get)
  }

  def apply[F[_]: Sync: Logger](db: Database[F]): F[PrivateConferenceService[F]] =
    Sync[F].delay(new Live[F](db))
}
