package ro.mihneabaia.api.model.base

import slick.lifted.MappedTo

/**
  * Base class to be extended for classes that represent type-safe IDs in the model
  */
abstract class ModelEntityId(value: Long) extends MappedTo[Long] {
  def apply() = value
}

/**
  * Base model entity
  */
trait BaseModelEntity[I <: ModelEntityId] {
  val id: Option[I]

  def isPersisted = id.isDefined
}

/**
  * Model entity with support for optimistic locking and time time auditing
  *
  * @tparam I
  */
trait ModelEntity[I <: ModelEntityId] extends BaseModelEntity[I] with Versionable with TimeAuditable

/**
  * Model entity with support for optimistic locking, time auditing and status
  *
  * @tparam I
  */
trait ModelEntityWithStatus[I <: ModelEntityId] extends ModelEntity[I] with Statusable

/**
  * Represents a page of data from a result including also the total number of entities available in the result
  */
case class ModelEntityPage[I <: ModelEntityId, M <: BaseModelEntity[I]](entities: Seq[M], total: Int)
