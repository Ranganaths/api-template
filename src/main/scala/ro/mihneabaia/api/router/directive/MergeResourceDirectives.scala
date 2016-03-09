package ro.mihneabaia.api.router.directive

import ro.mihneabaia.api.domain.base.{Resource, ResourceRead, Version}
import ro.mihneabaia.api.repository.DataAccess
import ro.mihneabaia.api.router.exception.{ResourceCannotBePartiallyUpdated, ResourceNotFoundException}
import spray.json.JsValue
import spray.routing.{Directive1, Directives}
import spray.json._
import ro.mihneabaia.api.utils.json.JsUtils._

import scala.concurrent.{ExecutionContext, Future}

trait  MergeResourceDirectives[R <: Resource] extends Directives {

  def mergedResource(partialResourceAsJsVal: JsValue, id: Long, version: Version, resourceRead: ResourceRead[R],
                     resourceName: String)(implicit dataAccess: DataAccess, ec: ExecutionContext, format: JsonFormat[R]): Directive1[Future[R]] =
    provide(resourceRead.read(id).map {
      case None => throw ResourceNotFoundException(resourceName, "partialUpdate", s"The resource with ID: $id was not found")
      case Some(res) => (jsonWriter[R].write(res), partialResourceAsJsVal) match {
        case (existing: JsObject, partialUpdate: JsObject) => jsonReader[R].read(existing ++ partialUpdate)
        case _ => throw ResourceCannotBePartiallyUpdated(resourceName,
          s"The partial data is incompatible with the resource representation")
      }
    })
}
