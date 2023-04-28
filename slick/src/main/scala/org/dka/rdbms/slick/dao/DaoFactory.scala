package org.dka.rdbms.slick.dao

import com.typesafe.config.ConfigFactory
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import slick.jdbc.JdbcBackend.Database
import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.dao.{AuthorDao, ConfigurationException, PublisherDao}
import org.dka.rdbms.slick.config.DBConfig
import org.dka.rdbms.slick.config.DBConfig._ // must be here even when intellij complains
import slick.util.AsyncExecutor

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
