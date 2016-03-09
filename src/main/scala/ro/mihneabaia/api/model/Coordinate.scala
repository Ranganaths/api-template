package ro.mihneabaia.api.model

import slick.lifted.MappedTo
import spray.json._
import JsonReader._
import JsonWriter._

case class Coordinate(degrees: BigDecimal) extends MappedTo[BigDecimal] {
  override def toString = value.toString

  def value = degrees

  def apply() = degrees
}

trait CoordinateJsonProtocol extends DefaultJsonProtocol {
  implicit val coordinateFormat: JsonFormat[Coordinate] =
    jsonFormat((json: JsValue) => Coordinate(json.convertTo[BigDecimal]), (c: Coordinate) => JsNumber(c()))
}
