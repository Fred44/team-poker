package flefebv.teamPoker

import flefebv.teamPoker.domain.DomainError
import flefebv.teamPoker.domain.game.GameId

import scala.util.{Failure, Success, Try}

package object application {

  type ErrorCode = String

  implicit class InfraErrorOps[S](infraResult: Try[S]) {
    def ifFailureThen(f: Throwable => ServiceError): Either[ServiceError, S] = {
      infraResult match {
        case Failure(e) => Left(f(e))
        case Success(s) => Right(s)
      }
    }
  }

  implicit class DomainErrorOps[E <: DomainError, R](domainResult: Either[E, R]) {
    def ifLeftThen(f: E => ServiceError): Either[ServiceError, R] = {
      domainResult match {
        case Left(e)  => Left(f(e))
        case Right(r) => Right(r)
      }
    }
  }

  implicit class TryOptionOps[T](maybeValue: Try[Option[T]]) {
    def ifNotExists(f: => ServiceError)(): Either[ServiceError, T] = {
      maybeValue match {
        case Success(Some(s)) => Right(s)
        case Success(None)    => Left(f)
        case Failure(e)       => Left(SystemError(e))
      }
    }
  }

  implicit class OptionOps[T](maybeValue: Option[T]) {
    def ifNotExists(f: => ServiceError)(): Either[ServiceError, T] = {
      maybeValue match {
        case Some(s)  => Right(s)
        case None     => Left(f)
      }
    }
  }

  def asServiceError[E](implicit f: E => ServiceError): E => ServiceError = f

  def asGameNotFoundError(gameId: GameId) = NotFoundError("POKER-GAME", gameId)
}
