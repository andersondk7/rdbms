package org.dka.rdbms.anorm.dao

import anorm.*
import anorm.SqlParser.*
import com.zaxxer.hikari.HikariDataSource
import org.dka.rdbms.common.dao.{CountryDao, CrudDao, ItemNotFoundException}
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.{CountryAbbreviation, CountryName, ID, Version}
import org.dka.rdbms.common.model.item.Country
import org.dka.rdbms.anorm.dao.*

import java.sql.Connection
import scala.util.Try
import scala.concurrent.{ExecutionContext, Future}

class CountryDaoImpl(dataSource: HikariDataSource, dbEx: ExecutionContext) extends CountryDao {
  override def create(item: Country)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Country]] = ???

  override def create(items: Seq[Country])(implicit ec: ExecutionContext): Future[DaoErrorsOr[Int]] = ???

  override def read(id: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Option[Country]]] = Future {
    implicit val connection: Connection = dataSource.getConnection
    Try {
      val q: SimpleSql[Row] = byIdQ(id)
      val result: Option[Country] = q.as(countryParser.singleOpt)
      Right(result)
    }.fold(
      ex => {
        println(s"caught $ex")
        connection.close()
        Left(ItemNotFoundException(id))
      },
      result => {
        connection.close()
        result
      })
  }(dbEx)

  override def delete(id: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Option[ID]]] = ???

  override def update(item: Country)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Country]] = ???

  //
  // queries
  //
  private val byIdQ: ID => SimpleSql[Row] = id => SQL"select * from countries where id = ${id.value.toString}"

  //
  // parsers
  //

  private val countryParser: RowParser[Country] = {
    getID ~ getVersion ~ getCountyName ~ getCountryAbbreviation map { case id ~ version ~ countryName ~ countryAbbreviation =>
        Country(id, version, countryName, countryAbbreviation)
      }
  }

  private def getCountyName: RowParser[CountryName] = get[String](CountryName.fieldName).map(CountryName.build)
  private def getCountryAbbreviation: RowParser[CountryAbbreviation] = get[String](CountryAbbreviation.fieldName).map(CountryAbbreviation.build)
}
