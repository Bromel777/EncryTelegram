package org.drinkless.tdlib

case class Handler[F[_]](resultHandler: ResultHandler[F],
                         exceptionHandler: Option[ExceptionHandler[F]] = None)
