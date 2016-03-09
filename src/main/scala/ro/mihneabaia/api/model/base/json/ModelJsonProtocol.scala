package ro.mihneabaia.api.model.base.json

import java.time.Instant

import ro.mihneabaia.api.model.base.{Status, Version, ModelEntityId}
import spray.json._

trait ModelJsonProtocol extends DefaultJsonProtocol {

  def modelEntityIdFormat[T<: ModelEntityId](cons: Long => T): JsonFormat[T] = {
    jsonFormat((json: JsValue) => cons(json.convertTo[Long]), (a: T) => JsNumber(a()))
  }

  implicit val versionFormat =
    jsonFormat((json: JsValue) => Version(json.convertTo[Long]), (a: Version) => JsNumber(a()))

  implicit val statusFormat =
    jsonFormat((json: JsValue) => Status(json.convertTo[Byte]), (s: Status) => JsNumber(s()))

  implicit val instantFormat =
    jsonFormat((json: JsValue) => Instant.parse(json.convertTo[String]), (i: Instant) => JsString(i.toString))
}
