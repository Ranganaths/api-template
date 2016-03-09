package ro.mihneabaia.api.model.base

import slick.lifted.MappedTo

/**
  * Used for mapping the version of a versionable model entity
  */
case class Version(value: Long) extends AnyVal with MappedTo[Long] {
  def increment() = copy(value + 1)

  def apply() = value
}

object Version {
  val first = Version(0)
}

/**
  * For optimistic locking support
  */
trait Versionable {
  val version: Option[Version]
}
