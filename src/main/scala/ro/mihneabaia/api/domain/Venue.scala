package ro.mihneabaia.api.domain

import ro.mihneabaia.api.domain.base._
import ro.mihneabaia.api.model.base.{Version => ModelVersion}
import ro.mihneabaia.api.model.{Venue => ModelVenue, VenueJsonProtocol => ModelVenueJsonProtocol, VenueId}
import ro.mihneabaia.api.repository.DataAccess
import ro.mihneabaia.api.validation.Validator
import spray.json.{JsValue, RootJsonFormat, DefaultJsonProtocol}

import scala.concurrent.{Future, ExecutionContext}

case class Venue(modelVenue: ModelVenue) extends Resource {
  val id = modelVenue.id.map(_.value)
  val version = modelVenue.version.map(v => Version(v.value))
}

case object Venue extends ResourceCRUD[Venue] {
  override def readPage(offset: Int, limit: Int)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[ResourcePage[Venue]] =
    dataAccess.db.run(
      for {
        result <- dataAccess.venues.listNotDeletedPaginated(offset, limit)
      } yield {
        ResourcePage(result.total, offset, limit, result.entities.map(Venue(_)))
      }
    )

  override def readAll(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Seq[Venue]] =
    dataAccess.db.run(
      dataAccess.venues.listNotDeleted.map(_.map(Venue(_)))
    )

  override def read(id: Long)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Option[Venue]] =
    dataAccess.db.run (
      dataAccess.venues.findNotDeleted(VenueId(id)).map(_.map(Venue(_)))
    )

  override def create(venue: Venue)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Venue] =
    dataAccess.db.run(
      for {
        venue <- dataAccess.venues.create(venue.modelVenue)
      } yield {
        Venue(venue)
      })

  override def delete(id: Long, version: Version)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Unit] =
    dataAccess.db.run(dataAccess.venues.deleteUsingStatus(VenueId(id), ModelVersion(version.value)).map(_ => Unit))

  override def update(venue: Venue, id: Long, version: Version)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Venue] =
    dataAccess.db.run(
      dataAccess.venues.modify(
        venue.modelVenue.copy(id = Some(VenueId(id)), version = Some(ModelVersion(version.value)))).map(Venue(_)))

  override def validateForCreate(venue: Venue)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Validator] =
    createUpdateValidation(venue)

  override def validateForUpdate(venue: Venue, version: Version)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Validator] =
    createUpdateValidation(venue)

  private def createUpdateValidation(venue: Venue)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Validator] =
    Validator().check("The name must be between 5 and 50 characters", Some("name")) {
      val nameLen = venue.modelVenue.name.length
      nameLen >= 5 && nameLen <= 50
    }.flatMap(_.check("The description must be between 10 and 100 characters", Some("description")) {
      val descrLen = venue.modelVenue.description.length
      descrLen >= 10 && descrLen <= 100
    })
}

trait VenueJsonProtocol extends DefaultJsonProtocol with ModelVenueJsonProtocol {

  implicit object VenueFormat extends RootJsonFormat[Venue] {
    def read(json: JsValue): Venue = {
      val jsObj = json.asJsObject
      Venue(jsObj.convertTo[ModelVenue])
    }

    def write(obj: Venue): JsValue = venueFormat.write(obj.modelVenue).asJsObject
  }

  implicit val venuePageFormat = new ResourcePageJsonProtocol[Venue].resourcePageFormat
}
