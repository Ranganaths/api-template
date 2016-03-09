package ro.mihneabaia.api.router.exception

import scala.runtime.ScalaRunTime

sealed abstract class ResourceCRUDException(val resourceName: String, val operation: String,
                                            val message: String, val cause: Option[Throwable]) extends RuntimeException

case class ResourceNotFoundException(override val resourceName: String,
                                     override val operation: String = "read",
                                     override val message: String = "The resource was not found",
                                     override val cause: Option[Throwable] = None)
  extends ResourceCRUDException(resourceName, operation, message, cause) {

  override def toString = ScalaRunTime._toString(this)
}

case class ResourceCannotBePartiallyUpdated(override val resourceName: String,
                                            override val message: String,
                                            override val cause: Option[Throwable] = None)
  extends ResourceCRUDException(resourceName, "partial update", message, cause) {

  override def toString = ScalaRunTime._toString(this)
}