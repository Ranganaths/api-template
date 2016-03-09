package ro.mihneabaia.api.model

import ro.mihneabaia.api.model.base.json.ModelJsonProtocol
import ro.mihneabaia.api.model.base.{ BaseModelEntity, ModelEntityId }

import ro.mihneabaia.api.utils.json.JsUtils._

case class ContactId(value: Long) extends ModelEntityId(value)

case class Contact(
  id: Option[ContactId],
  email: Option[String],
  address: Option[String],
  phone: Option[String],
  latitude: Option[Coordinate],
  longitude: Option[Coordinate]) extends BaseModelEntity[ContactId]

trait ContactJsonProtocol extends ModelJsonProtocol with CoordinateJsonProtocol {

  implicit val contactIdFormat = modelEntityIdFormat(ContactId)

  // hide the id from the json representation because contact will be part of other resources
  implicit val contactFormat = jsonFormat6(Contact) - "id"

}