package ro.mihneabaia.api.validation

case class ValidationError(errorMessage: String, fieldPath: Option[String] = None)
