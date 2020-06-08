package org.encryfoundation.tg.utils

import cats.FlatMap
import tofu.common.Console
import tofu.syntax.console._
import tofu.syntax.monadic._

object ConsoleUtils {

  def grabCommand[F[_]: FlatMap: Console]: F[String] = for {
    _ <- putStrLn("Write command.")
    command <- readStrLn
    _ <- putStrLn(s"Your command: $command")
  } yield command

}
