package ro.mihneabaia.api.model.base.exception

case class DataConstraintViolation(msg: String, cause: Throwable) extends RuntimeException(msg, cause)
