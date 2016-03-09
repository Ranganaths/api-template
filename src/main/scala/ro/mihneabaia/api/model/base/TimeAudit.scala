package ro.mihneabaia.api.model.base

import java.time.Instant

/**
  * Time audit support for our data: creation and latest modification timestamps
  */
trait TimeAuditable {
  val created: Option[Instant]
  val modified: Option[Instant]
}
