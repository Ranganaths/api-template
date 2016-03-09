package ro.mihneabaia.api.router.rejection

import akka.event.LoggingAdapter
import spray.http.StatusCodes._
import spray.routing._

object DefaultRejectionHandler extends Directives {
  def apply(logger: LoggingAdapter) = RejectionHandler {
    case (r: UnsupportedOperationRejection) :: _ =>
      complete(MethodNotAllowed, r.toString)
  }
}
