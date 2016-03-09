package ro.mihneabaia.api.repository

import org.slf4j.LoggerFactory
import ro.mihneabaia.api.repository.base.AppConfigDbAware
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}

object DbHelper extends AppConfigDbAware with DataAccess {
  private val log = LoggerFactory.getLogger(getClass)

  import profile.api._

  lazy val schema = arenas.schema ++ venues.schema ++ contacts.schema

  def create() = db.run(schema.create) onComplete {
    case Success(_) => log.info("Database schema successfully created")
    case Failure(ex) => log.error("Database create failed - {}", ex.getMessage)
  }

  def drop() = db.run(schema.drop) onComplete {
    case Success(_) => log.info("Database schema successfully dropped")
    case Failure(ex) => log.error("Database drop failed - {}", ex.getMessage)
  }
}
