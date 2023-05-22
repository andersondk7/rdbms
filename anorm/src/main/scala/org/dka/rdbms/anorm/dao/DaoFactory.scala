package org.dka.rdbms.anorm.dao

import com.typesafe.scalalogging.Logger
import com.zaxxer.hikari.util.UtilityElf
import com.zaxxer.hikari.util.UtilityElf.DefaultThreadFactory
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.dka.rdbms.common.config.DBConfig
import org.dka.rdbms.common.config.DBConfig.ConfigErrorsOr
import org.dka.rdbms.common.dao.CountryDao

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue, Executors, ThreadPoolExecutor, TimeUnit}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*

class DaoFactory(val dataSource: HikariDataSource, dbEx: ExecutionContext) {
  val countryDao: CountryDao = new CountryDaoImpl(dataSource, dbEx)
}

object DaoFactory {
  private val logger = Logger(getClass.getName)
  lazy val configure: ConfigErrorsOr[DaoFactory] = {
    logger.info(s"loading configure")
//    Class.forName("org.postgresql.ds.PGSimpleDataSource")
    DBConfig.load
      .map { config =>
        val poolConfig = new HikariConfig()
        poolConfig.setJdbcUrl(config.url)
        poolConfig.setUsername(config.properties.user)
        poolConfig.setPassword(config.properties.password)
        poolConfig.setMaximumPoolSize(config.maxConnections)
        poolConfig.setPoolName(config.connectionPool)
        // not really needed since defaults to "org.postgresql.Driver"
//        poolConfig.setDriverClassName(config.dataSourceClass)
        val workingQueue = new ArrayBlockingQueue(config.queueSize, true).asInstanceOf[BlockingQueue[Runnable]]
        val dbEx: ExecutionContext = ExecutionContext.fromExecutor(
          new ThreadPoolExecutor(config.numThreads, config.numThreads, 0L, TimeUnit.MILLISECONDS, workingQueue)
        )
        val dataSource: HikariDataSource = new HikariDataSource(poolConfig)
        val factory = new DaoFactory(dataSource, dbEx)
        logger.info(s"got factory")
        factory
      }

  }
}
