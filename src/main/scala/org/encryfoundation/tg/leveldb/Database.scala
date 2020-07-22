package org.encryfoundation.tg.leveldb

import java.io.File

import cats.Applicative
import cats.effect.{Resource, Sync}
import cats.implicits._
import org.iq80.leveldb.{DB, Options, ReadOptions}
import scorex.crypto.hash.Blake2b256

trait Database[F[_]] {
  def put(key: Array[Byte], value: Array[Byte]): F[Unit]
  def get(key: Array[Byte]): F[Option[Array[Byte]]]
  def remove(key: Array[Byte]): F[Unit]
  def getAllKeys(): F[List[Array[Byte]]]
}

object Database {

  val privateGroupChatsKey = Blake2b256.hash("privateGroupChatsKey")

  final private case class Live[F[_]: Sync](db: DB) extends Database[F] {

    override def get(key: Array[Byte]): F[Option[Array[Byte]]] = Sync[F].delay {
      val readOptions = new ReadOptions()
      readOptions.snapshot(db.getSnapshot)
      try {
        val res = db.get(key)
        if (res == null) Option.empty[Array[Byte]] else res.some
      } finally readOptions.snapshot().close()
    }

    override def put(key: Array[Byte], value: Array[Byte]): F[Unit] = Sync[F].delay(db.put(key, value))

    override def remove(key: Array[Byte]): F[Unit] = Sync[F].delay(db.delete(key))

    override def getAllKeys(): F[List[Array[Byte]]] = Sync[F].delay {
      val readOptions = new ReadOptions()
      readOptions.snapshot(db.getSnapshot)
      val iter = db.iterator(readOptions)
      try {
        iter.seekToFirst()
        var buffer: List[Array[Byte]] = List.empty[Array[Byte]]
        while (iter.hasNext) {
          val nextKey = iter.next().getKey
          buffer ::= nextKey
        }
        buffer
      } finally {
        iter.close()
        readOptions.snapshot().close()
      }
    }
  }

  def apply[F[_]: Sync](dir: File): Resource[F, Database[F]] = {
    for {
      factory <- Resource.liftF(LevelDbFactory.factory[F])
      db <- Resource.make(Sync[F].delay(factory.open(dir, new Options())))(db => Sync[F].delay(db.close()))
    } yield Live[F](db)
  }
}