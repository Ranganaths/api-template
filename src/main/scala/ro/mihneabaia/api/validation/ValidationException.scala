package ro.mihneabaia.api.validation

case class ValidationException(errors: Seq[ValidationError]) extends RuntimeException
