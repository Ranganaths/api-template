package ro.mihneabaia.api.domain.base

import spray.json.{JsonFormat, DefaultJsonProtocol}

case class ResourcePage[R <: Resource](totalCount: Int, from: Int, size: Int, items: Seq[R])

class ResourcePageJsonProtocol[R <: Resource](implicit resourceFormat: JsonFormat[R]) extends DefaultJsonProtocol {
  implicit val resourcePageFormat = jsonFormat4(ResourcePage[R])
}
