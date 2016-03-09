package ro.mihneabaia.api.domain.base

case class Version(value: Long) extends AnyVal

trait Resource {
  val id: Option[Long]
  val version: Option[Version]
}
