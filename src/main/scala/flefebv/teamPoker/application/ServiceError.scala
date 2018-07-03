package flefebv.teamPoker.application

import flefebv.teamPoker.domain.EntityId
import flefebv.teamPoker.domain.game.PokerGame

abstract sealed class ServiceError(val errorCode: ErrorCode, val args: Any*) {

  protected val stackTrace = {
    val traces = Thread.currentThread().getStackTrace
    traces.drop(traces.lastIndexWhere(t => t.getClassName == getClass.getName) + 1)
  }

  override def toString = {
    s"""${getClass.getName}($errorCode, [${args.mkString(", ")}])
       |${stackTrace.map(s => s"  at $s").mkString("\n")}
    """.stripMargin
  }
}

case class SystemError(cause: Throwable) extends ServiceError(ServiceErrorCodes.SystemError)

abstract class ApplicationError(errorCode: ErrorCode, args: Any*) extends ServiceError(errorCode, args: _*)

case class NotFoundError(entityType: String, id: EntityId)
  extends ApplicationError(ServiceErrorCodes.NotFound, entityType, id)

case class IllegalGameOperationError(game: PokerGame)
  extends ApplicationError(ServiceErrorCodes.InvalidGameOperation, game.id, game.state)

object ServiceErrorCodes {

  val SystemError = "error.system"
  val NotFound    = "error.notFound"

  // in GameService
  val InvalidGameOperation = "error.invalidGameOperation"
}