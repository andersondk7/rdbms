package org.dka.rdbms.slick.dao

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.dao.ConfigurationException
import org.dka.rdbms.slick.config.DBConfig
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import slick.jdbc.JdbcBackend.Database
import slick.util.AsyncExecutor

import scala.util.{Failure, Try} // must be kept even though intellij thinks it is unused

/**
 * Factory to create all the dao implementations for the project
 * @param database
 *   database that the dao's will use
 */
class DaoFactory(val database: Database) {
  import org.dka.rdbms.slick.config.DBConfig._ // keep inspite of intellij
  val countryDao: CountryDaoImpl = new CountryDaoImpl(database)
  val locationDao: LocationDaoImpl = new LocationDaoImpl(database)
  val authorDao: AuthorDaoImpl = new AuthorDaoImpl(database)
  val publisherDao: PublisherDaoImpl = new PublisherDaoImpl(database)
  val bookDao: BookDaoImpl = new BookDaoImpl(database)
  val authorsTitlesDao: AuthorsBooksDao = new AuthorsBooksDao(database)
}

/**
 * builds a DaoFactory there will be only one DaoFactory created (via a lazy val)
 *
 * reads the config to create a database and uses that database to construct the DaoFactory
 *
 * This builder is expected to be called by the client of the library (which could be tests or an application)
 */
object DaoFactoryBuilder {
  import org.dka.rdbms.slick.config.DBConfig._
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
          // executor construction lifted from Database.forConfig()
          val executor = AsyncExecutor(
            config.connectionPool,
            config.numThreads,
            config.numThreads,
            config.queueSize,
            config.maxConnections,
            config.registerMBeans
          )
          val db = Database.forURL(executor = executor, url = config.url)
          val factory = new DaoFactory(db)
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
