package ro.mihneabaia.api.repository.base

import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
  * Add support for accessing the JdbcProfile
  */
trait JdbcProfileAware {
  val profile: JdbcProfile
}

/**
  * Add support for db access using JdbcProfile
  */
trait DbAware extends JdbcProfileAware {
  val db: JdbcProfile#Backend#Database
}

/**
  * Add support for accessing a database configured in the application config
  */
trait AppConfigDbAware extends DbAware {
  val config = DatabaseConfig.forConfig[JdbcProfile]("api.database")

  val profile = config.driver
  val db = config.db
}