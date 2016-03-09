package ro.mihneabaia.api.router

import ro.mihneabaia.api.domain.{Venue, VenueJsonProtocol}
import ro.mihneabaia.api.repository.DataAccess
import ro.mihneabaia.api.router.base.RouteBuilder

import scala.concurrent.ExecutionContext

trait VenueRouter extends VenueJsonProtocol {

  implicit def nonDbExecutionContext: ExecutionContext

  implicit def dataAccess: DataAccess

  val venueRoutesBuilder =
    RouteBuilder[Venue]("venue")
      .withCreateRoute(Venue)
      .withUpdateRoute(Venue)
      .withReadRoute(Venue)
      .withReadAllRoute(Venue)
      .withReadPageRoute(Venue)
      .withDeleteRoute(Venue)
      .withPartialUpdateRoute(Venue)

}
