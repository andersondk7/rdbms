package org.dka.rdbms.dao

import com.typesafe.config.ConfigFactory
import org.dka.rdbms.config.DBConfig
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import slick.jdbc.JdbcBackend.Database
import DBConfig._
import com.typesafe.scalalogging.Logger
import org.dka.rdbms.model.{AuthorDao, PublisherDao}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Try} // must be kept even though intellij thinks it is unused

class DaoFactory(val database: Database) {
  val authorsDao: AuthorDao = new AuthorDaoImpl(database)
  val publisherDao: PublisherDao = new PublisherDaoImpl(database)
}

object DaoFactoryBuilder {
  private val logger = Logger(getClass.getName)
  lazy val configure: Either[ConfigurationException, DaoFactory] = {
    try {
      logger.info(s"loading configure")
      ConfigSource
        .fromConfig(
          ConfigFactory.load().getConfig("DBConfig") // just want this piece of the config file
        )
        .load[DBConfig]
        .left
        .map(errors => ConfigurationException(errors.toList.map(f => f.toString)))
        .map { config =>
          logger.info(s"config: $config")
          logger.info(s"url: ${config.url}")
          // todo enable connection pool, compare this code with Database.forConfig()
          val factory = new DaoFactory(Database.forURL(config.url))
          logger.info(s"got factory")
          factory
        }
    } catch {
      case t: Throwable => Left(ConfigurationException(List(t.getMessage)))
    }
  }

  def shutdown(database: Database): Try[Unit] = Try {
    database.close()
  }.recoverWith { case t: Throwable =>
    Failure(ConfigurationException(List(t.getMessage), Some(t)))
  }

}
