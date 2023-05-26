package org.dka.rdbms.anorm.dao

import com.zaxxer.hikari.HikariDataSource

import java.sql.Connection
import scala.concurrent.{ExecutionContext, Future}

trait DB {

  def dataSource: HikariDataSource

  def withConnection[A](dbEx: ExecutionContext)(block: Connection => A): Future[A] = Future {
    val connection = dataSource.getConnection
    val result: A  = block(connection)
    connection.close()
    result
  }(dbEx)

  //
  // work in progress
  // this does not work!!
  // needs more investigation
  // please see CrudDaoImpl:update, which does work in a transaction
  //
  def withTransaction[A](block: Connection => A): A = {
    val connection = dataSource.getConnection
    connection.setAutoCommit(false)
    // these type of transaction boundary does not block until the row is released, it blows up
    // only these levels work:
    // Transaction_Repeatable_Read
    // Transaction_Serializable
    connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ)
    val result: A = block(connection)
    connection.commit()
    connection.close()
    result
  }

}
