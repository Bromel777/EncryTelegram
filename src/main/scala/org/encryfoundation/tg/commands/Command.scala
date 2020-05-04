package org.encryfoundation.tg.commands

trait Command[F[_]] {

  val name: String

  def run(args: List[String]): F[Unit]
}
