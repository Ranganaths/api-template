package ro.mihneabaia.api.validation

import scala.concurrent.{ExecutionContext, Future}

case class Validator(errors: List[ValidationError]) {
  def isEmpty = errors.isEmpty

  def checkF(message: String, path: Option[String] = None)(isValid: =>Future[Boolean])(implicit ec: ExecutionContext): Future[Validator] =
    isValid.map {
      case valid: Boolean =>
        if (!valid)
          Validator(ValidationError(message, path) :: errors)
        else
          this
    }

  def check(message: String, path: Option[String] = None)(isValid: =>Boolean)(implicit ec: ExecutionContext): Future[Validator] =
    checkF(message, path)(Future.successful(isValid))
}

object Validator {
  def apply(): Validator = Validator(Nil)
}
