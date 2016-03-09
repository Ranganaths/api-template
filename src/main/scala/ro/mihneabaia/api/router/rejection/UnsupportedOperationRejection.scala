package ro.mihneabaia.api.router.rejection

import spray.routing.Rejection

sealed abstract class UnsupportedOperationRejection(val resourceName: String, val operation: String) extends Rejection {
  override def toString = s"The $resourceName resource does not support '${operation}' operation"
}

case class UnsupportedReadAllOperationRejection(override val resourceName: String)
  extends UnsupportedOperationRejection(resourceName, "read all resources")

case class UnsupportedReadOperationRejection(override val resourceName: String)
  extends UnsupportedOperationRejection(resourceName, "read one resource")

case class UnsupportedReadPageOperationRejection(override val resourceName: String)
  extends UnsupportedOperationRejection(resourceName, "read page")

case class UnsupportedCreateOperationRejection(override val resourceName: String)
  extends UnsupportedOperationRejection(resourceName, "create")

case class UnsupportedUpdateOperationRejection(override val resourceName: String)
  extends UnsupportedOperationRejection(resourceName, "update")

case class UnsupportedPartialUpdateOperationRejection(override val resourceName: String)
  extends UnsupportedOperationRejection(resourceName, "partial update")

case class UnsupportedDeleteOperationRejection(override val resourceName: String)
  extends UnsupportedOperationRejection(resourceName, "delete")
