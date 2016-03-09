package ro.mihneabaia.api.model.base

import slick.lifted.MappedTo

sealed class Status(val value: Byte) extends MappedTo[Byte] {
  override def toString = value.toString

  def apply() = value
}

object Status {
  val active = new Status(1)
  val disabled = new Status(2)
  val deleted = new Status(3)

  def apply(value: Byte): Status = value match {
    case active.value => active
    case disabled.value => disabled
    case deleted.value => deleted
    case v => throw new RuntimeException(s"Unknown status [$v]")
  }
}

trait Statusable {
  val status: Status
}