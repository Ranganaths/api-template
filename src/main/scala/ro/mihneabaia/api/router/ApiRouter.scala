package ro.mihneabaia.api.router

import ro.mihneabaia.api.boot.{LoggerAware, ActorContextAware}
import ro.mihneabaia.api.repository.DataAccess
import ro.mihneabaia.api.router.exception.DefaultExceptionHandler
import ro.mihneabaia.api.router.rejection.DefaultRejectionHandler
import spray.routing.RouteConcatenation

trait ApiRouter extends ArenaRouter with VenueRouter with RouteConcatenation with LoggerAware with DataAccess {
  self: ActorContextAware =>

  // execution context used by slick to run the non-db code on, when composing db actions
  lazy val nonDbExecutionContext = context.system.dispatchers.lookup("api.non-db-execution-context")

  lazy val dataAccess: DataAccess = this

  implicit val exceptionHandler = DefaultExceptionHandler(logger)

  implicit val rejectionHandler = DefaultRejectionHandler(logger)

  val apiRoutes = arenaRoutesBuilder() ~ venueRoutesBuilder()
}
