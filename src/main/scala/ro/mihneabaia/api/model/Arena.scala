package ro.mihneabaia.api.model

import ro.mihneabaia.api.model.base.json.ModelJsonProtocol
import java.time.Instant

import ro.mihneabaia.api.model.base._
import ro.mihneabaia.api.utils.json.JsUtils._

case class ArenaId(value: Long) extends ModelEntityId(value)

case class Arena(
  id: Option[ArenaId],
  version: Option[Version],
  created: Option[Instant],
  modified: Option[Instant],
  status: Status,
  name: String,
  description: String,
  idContact: Option[ContactId]) extends ModelEntityWithStatus[ArenaId] {

  def this(name: String, description: String) = this(None, None, None, None, Status.active, name, description, None)
}

trait ArenaJsonProtocol extends ModelJsonProtocol with ContactJsonProtocol {
  implicit val arenaIdFormat = modelEntityIdFormat(ArenaId)

  implicit val arenaFormat = jsonFormat8(Arena) - "idContact"
}
