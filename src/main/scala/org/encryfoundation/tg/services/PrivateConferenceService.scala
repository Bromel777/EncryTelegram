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
