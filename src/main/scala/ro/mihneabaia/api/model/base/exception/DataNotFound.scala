package ro.mihneabaia.api.model.base.exception

case class DataNotFound(msg: String) extends RuntimeException(msg)
