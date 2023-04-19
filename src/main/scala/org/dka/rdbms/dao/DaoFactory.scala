package org.dka.rdbms.dao

import com.typesafe.config.ConfigFactory
import org.dka.rdbms.config.DBConfig
import org.dka.rdbms.model.ConfigurationException
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import slick.jdbc.JdbcBackend.Database
import DBConfig._
import com.typesafe.scalalogging.Logger  // must be kept even though intellij thinks it is unused

class DaoFactory(private val database: Database) {

  val authorsDao: Authors = new Authors(database)

}

object DaoFactoryBuilder {
  private val logger = Logger("DaoFactoryBuilder")
  def configure: Either[ConfigurationException, DaoFactory] = {
    // read the config directly to make sure it is configured properly
    // the Database.forConfig does not give readable errors ...

    try {
      ConfigSource
        .fromConfig(
          ConfigFactory.load().getConfig("DBConfig") // just want this piece of the config file
        )
        .load[DBConfig]
        .left.map(errors => ConfigurationException(errors.toList.map(f => f.toString)))
        .map(config => {
          logger.info(s"config: $config")
          logger.info(s"url: ${config.url}")
          // another option is to convert the DBConfig instance to a typesafe config which overrides the password
//          new DaoFactory(Database.forConfig("DBConfig"))

          new DaoFactory(Database.forURL(config.url))
        })
    }
    catch {
      case t: Throwable => Left(ConfigurationException(List(t.getMessage)))
    }
  }
}


