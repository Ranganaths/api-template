package ro.mihneabaia.api.model.base.exception

import ro.mihneabaia.api.model.base.{BaseModelEntity, ModelEntityId}

case class StaleDataException[I <: ModelEntityId](msg: String, data: Option[BaseModelEntity[I]] = None)
  extends RuntimeException(msg) {

  override def toString = s"${super.toString}${data.map(". New data would be: " + _.toString).getOrElse("")}"
}
