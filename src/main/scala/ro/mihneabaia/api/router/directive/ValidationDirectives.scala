package ro.mihneabaia.api.router.directive

import ro.mihneabaia.api.validation.{Validator, ValidationException}
import shapeless.HNil
import spray.routing._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait ValidationDirectives extends Directives {
  def validate(validatorF: =>Future[Validator])(implicit ec: ExecutionContext): Directive0 = new Directive0 {
    override def happly(f: (HNil) => Route): Route =
      onComplete(validatorF) {
        case Success(validator) if validator.isEmpty => f(HNil)
        case Success(validator) => failWith(ValidationException(validator.errors))
        case Failure(ex) => failWith(ex)
      }
  }
}
