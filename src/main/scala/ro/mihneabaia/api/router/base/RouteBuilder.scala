package ro.mihneabaia.api.router.base

import ro.mihneabaia.api.domain.base._
import ro.mihneabaia.api.repository.DataAccess
import ro.mihneabaia.api.router.directive.{ValidationDirectives, MergeResourceDirectives, VersionDirectives}
import ro.mihneabaia.api.router.rejection._
import spray.http.HttpHeaders.Location
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.routing.{MissingQueryParamRejection, Directives, Route}

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}

case class InnerRoutes[R](create: Option[Route] = None, read: Option[Long => Route] = None,
                          readAll: Option[Route] = None, readPage: Option[(Int, Int) => Route] = None,
                          update: Option[Long => Route] = None, partialUpdate: Option[Long => Route] = None,
                          delete: Option[Long => Route] = None, customRoutes: List[Route] = Nil)

case class RouteBuilder[R <: Resource](resourceName: String, innerRoutes: InnerRoutes[R] = InnerRoutes[R]())
                                      (implicit dataAccess: DataAccess, ec: ExecutionContext)
  extends Directives with VersionDirectives with MergeResourceDirectives[R] with ValidationDirectives
    with SprayJsonSupport with DefaultJsonProtocol {

  def apply() = routes

  def withReadAllRoute(resourceReadAll: ResourceReadAll[R])(implicit jsonWriter: RootJsonWriter[R]): RouteBuilder[R] =
    this.copy(innerRoutes = innerRoutes.copy(readAll = Some(readAllRoute(resourceReadAll))))

  def withReadPageRoute(resourceReadPage: ResourceReadPage[R])(implicit jsonWriter: RootJsonWriter[ResourcePage[R]]): RouteBuilder[R] =
    this.copy(innerRoutes = innerRoutes.copy(readPage = Some(readPageRoute(resourceReadPage))))

  def withReadRoute(resourceRead: ResourceRead[R])(implicit jsonWriter: RootJsonWriter[R]): RouteBuilder[R] =
    this.copy(innerRoutes = innerRoutes.copy(read = Some(readRoute(resourceRead))))

  def withCreateRoute(resourceCreate: ResourceCreate[R])(implicit jsonFormat: RootJsonFormat[R]): RouteBuilder[R] =
    this.copy(innerRoutes = innerRoutes.copy(create = Some(createRoute(resourceCreate))))

  def withUpdateRoute(resourceUpdate: ResourceUpdate[R])(implicit jsonFormat: RootJsonFormat[R]): RouteBuilder[R] =
    this.copy(innerRoutes = innerRoutes.copy(update = Some(updateRoute(resourceUpdate))))

  def withPartialUpdateRoute(resourceReadAndUpdate: ResourceUpdate[R] with ResourceRead[R])(implicit format: RootJsonFormat[R]): RouteBuilder[R] =
    this.copy(innerRoutes = innerRoutes.copy(
      partialUpdate = Some(partialUpdateRoute(resourceReadAndUpdate, resourceReadAndUpdate))))

  def withPartialUpdateRoute(resourceUpdate: ResourceUpdate[R], resourceRead: ResourceRead[R])(implicit format: RootJsonFormat[R]): RouteBuilder[R] =
    this.copy(innerRoutes = innerRoutes.copy(partialUpdate = Some(partialUpdateRoute(resourceUpdate, resourceRead))))

  def withDeleteRoute(resourceDelete: ResourceDelete): RouteBuilder[R] =
    this.copy(innerRoutes = innerRoutes.copy(delete = Some(deleteRoute(resourceDelete))))

  def withCustomRoute(route: Route): RouteBuilder[R] =
    this.copy(innerRoutes = innerRoutes.copy(customRoutes = route :: innerRoutes.customRoutes))

  private def readAllRoute(resourceReadAll: ResourceReadAll[R])(implicit jsonWriter: RootJsonWriter[R]) =
    respondWithMediaType(`application/json`) {
      onComplete(resourceReadAll.readAll) {
        case Success(results) => complete(results.map(_.toJson))
        case Failure(ex) => failWith(ex)
      }
    }

  private def readPageRoute(resourceReadPage: ResourceReadPage[R])(from: Int, size: Int)(implicit jsonWriter: RootJsonWriter[ResourcePage[R]]) =
    respondWithMediaType(`application/json`) {
      onComplete(resourceReadPage.readPage(from, size)) {
        case Success(results) => complete(results)
        case Failure(ex) => failWith(ex)
      }
    }

  private def readRoute(resourceRead: ResourceRead[R])(implicit jsonWriter: RootJsonWriter[R]): (Long) => Route = {
    id: Long =>
      respondWithMediaType(`application/json`) {
        onComplete(resourceRead.read(id)) {
          case Success(resource) => complete(resource)
          case Failure(ex) => failWith(ex)
        }
      }
  }

  private def createRoute(resourceCreate: ResourceCreate[R])(implicit jsonFormat: RootJsonFormat[R]) =
    entity(as[R]) { resource =>
      validate(resourceCreate.validateForCreate(resource))(ec) {
        requestUri { uri =>
          respondWithMediaType(`application/json`) {
            onComplete(resourceCreate.create(resource)) {
              case Success(resource) => {
                respondWithHeader(Location(s"$uri/${resource.id.get}")) {
                  addResourceVersionToResponse(resource) {
                    complete((Created, resource))
                  }
                }
              }
              case Failure(ex) => failWith(ex)
            }
          }
        }
      }
    }

  private def updateRoute(resourceUpdate: ResourceUpdate[R])(implicit jsonFormat: RootJsonFormat[R]): Long => Route = {
    id: Long =>
      entity(as[R]) { resource =>
        resourceVersion { version =>
          validate(resourceUpdate.validateForUpdate(resource, version))(ec) {
            respondWithMediaType(`application/json`) {
              onComplete(resourceUpdate.update(resource, id, version)) {
                case Success(res) => {
                  addResourceVersionToResponse(res) {
                    complete(res)
                  }
                }
                case Failure(ex) => failWith(ex)
              }
            }
          }
        }
      }
  }

  private def partialUpdateRoute(resourceUpdate: ResourceUpdate[R], resourceRead: ResourceRead[R])
                                (implicit format: RootJsonFormat[R]): Long => Route = {
    id: Long =>
      entity(as[JsObject]) { partialResAsJsVal =>
        resourceVersion { version =>
          mergedResource(partialResAsJsVal, id, version, resourceRead, resourceName)(dataAccess, ec, format) { resourceToUpdate: Future[R] =>
            validate(resourceToUpdate.flatMap(res => resourceUpdate.validateForUpdate(res, version)))(ec) {
              respondWithMediaType(`application/json`) {
                onComplete(resourceToUpdate.flatMap(resourceUpdate.update(_, id, version))) {
                  case Success(res) => {
                    addResourceVersionToResponse(res) {
                      complete(res)
                    }
                  }
                  case Failure(ex) => failWith(ex)
                }
              }
            }
          }
        }
      }
  }

  private def deleteRoute(resourceDelete: ResourceDelete): Long => Route = {
    id: Long =>
      resourceVersion { version =>
        respondWithMediaType(`application/json`) {
          onComplete(resourceDelete.delete(id, version)) {
            case Success(res) => {
              complete(NoContent)
            }
            case Failure(ex) => failWith(ex)
          }
        }
      }
  }

  val routes = pathPrefix(resourceName) {
    get {
      path(LongNumber) { resourceId =>
        innerRoutes.read match {
          case None => reject(UnsupportedReadOperationRejection(resourceName))
          case Some(r) => r(resourceId)
        }
      } ~
      pathEndOrSingleSlash {
        parameters ('from.as[Option[Int]], 'size.as[Int] ? 10 ) { (from, size) =>
          innerRoutes.readPage match {
            case None => from match {
              case None => reject
              case _ => reject(UnsupportedReadPageOperationRejection(resourceName))
            }
            case Some(r) => from match {
              case Some(f) => r(f, size)
              case None => reject(MissingQueryParamRejection("from"))
            }
          }
        } ~
        (innerRoutes.readAll match {
          case None => reject(UnsupportedReadAllOperationRejection(resourceName))
          case Some(r) =>
            parameters ('from.as[Option[Int]]) {
              case None => r
              case _ => reject
            }
        })
      }
    } ~
    post {
      innerRoutes.create match {
        case None => reject(UnsupportedCreateOperationRejection(resourceName))
        case Some(r) => r
      }
    } ~
    put {
      path(LongNumber) { resourceId =>
        innerRoutes.update match {
          case None => reject(UnsupportedUpdateOperationRejection(resourceName))
          case Some(r) => r(resourceId)
        }
      }
    } ~
    patch {
      path(LongNumber) { resourceId =>
        innerRoutes.partialUpdate match {
          case None => reject(UnsupportedUpdateOperationRejection(resourceName))
          case Some(r) => r(resourceId)
        }
      }
    } ~
    delete {
      path(LongNumber) { resourceId =>
        innerRoutes.delete match {
          case None => reject(UnsupportedDeleteOperationRejection(resourceName))
          case Some(r) => r(resourceId)
        }
      }
    }
  }
}
