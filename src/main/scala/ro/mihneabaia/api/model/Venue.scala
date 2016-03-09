package ro.mihneabaia.api.model

import java.time.Instant

import ro.mihneabaia.api.model.base._
import ro.mihneabaia.api.model.base.json.ModelJsonProtocol

case class VenueId(value: Long) extends ModelEntityId(value)

case class Venue(
  id: Option[VenueId],
  version: Option[Version],
  created: Option[Instant],
  modified: Option[Instant],
  arenaId: ArenaId,
  status: Status,
  name: String,
  description: String) extends ModelEntityWithStatus[VenueId] {

  def this(arenaId: Long, name: String, description: String) = this(None, None, None, None, ArenaId(arenaId), Status.active, name, description)
}

trait VenueJsonProtocol extends ModelJsonProtocol with ArenaJsonProtocol {
  implicit val venueIdFormat = modelEntityIdFormat(VenueId)

  implicit val venueFormat = jsonFormat8(Venue)
}
