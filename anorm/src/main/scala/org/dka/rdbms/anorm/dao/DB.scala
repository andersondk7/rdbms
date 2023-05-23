package org.dka.rdbms.anorm.dao

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

trait DB {
  def dataSource: HikariDataSource
  def withConnection[A](block: Connection => A): A = {
    val connection = dataSource.getConnection
    val result: A = block(connection)
    connection.close()
    result
  }
}
