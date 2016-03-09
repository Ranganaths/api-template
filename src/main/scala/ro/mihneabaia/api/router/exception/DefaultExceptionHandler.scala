package ro.mihneabaia.api.router.exception

import akka.event.LoggingAdapter
import ro.mihneabaia.api.model.base.exception.{DataConstraintViolation, DataNotFound, StaleDataException}
import ro.mihneabaia.api.validation.ValidationException
import spray.http.StatusCodes._
import spray.routing._

object DefaultExceptionHandler extends Directives {
  def apply(logger: LoggingAdapter) = ExceptionHandler {
    case e: StaleDataException[_] =>
      logger.debug(e.toString)
      complete(PreconditionFailed, "The resource you are trying to change was modified. " +
        "Update your representation of the resource you are trying to change before attempting to modify it.")

    case e: DataNotFound =>
      logger.debug(e.toString)
      complete(NotFound, "The resource you are trying to access was not found.")

    case e@ResourceNotFoundException(resourceName, operation, message, cause) =>
      logger.debug(e.toString)
      complete(NotFound, message)

    case e@DataConstraintViolation(message, cause) =>
      logger.debug(e.toString)
      complete(Conflict, s"A data constraint was violated: $message")

    case e@ValidationException(errors) =>
      logger.debug(e.toString)
      complete(BadRequest,
        s"The following validation errors were encountered: ${errors.map(err =>
          s"[Message: '${err.errorMessage}'${err.fieldPath.fold("")(p => s", field path: '${p}'")}]").mkString(", ")}")
  }
}
