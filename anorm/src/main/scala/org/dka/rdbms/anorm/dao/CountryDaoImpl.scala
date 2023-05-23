package org.dka.rdbms.anorm.dao

import anorm.*
import anorm.SqlParser.*
import com.typesafe.scalalogging.Logger
import com.zaxxer.hikari.HikariDataSource
import org.dka.rdbms.common.dao.{CountryDao, CrudDao, DeleteException, InsertException, ItemNotFoundException}
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.{CountryAbbreviation, CountryName, ID, Version}
import org.dka.rdbms.common.model.item.Country
import org.dka.rdbms.anorm.dao.*

import java.sql.Connection
import scala.util.Try
import scala.concurrent.{ExecutionContext, Future}

class CountryDaoImpl(override val dataSource: HikariDataSource, dbEx: ExecutionContext) extends CountryDao with DB {
  import CountryDaoImpl.*
  private val logger = Logger(getClass.getName)
  override def create(item: Country)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Country]] = Future {
    withConnection { implicit connection: Connection =>
      Try {
        val q = insertQ(item)
        q.execute()
      }.fold(
        ex => {
          logger.warn(s"could not insert $item, because $ex")
          Left(InsertException(s"could not insert $item", Some(ex)))
        }
        ,
        _ => Right(item)
      )
    }
  }

  override def create(items: Seq[Country])(implicit ec: ExecutionContext): Future[DaoErrorsOr[Int]] = Future {
    withConnection { implicit connection: Connection =>
      Try {
        val q = insertMultipleQ(items)
        q.execute()
      }.fold(
        ex => {
          logger.warn(s"could not insert ${items.size}, because $ex")
          Left(InsertException(s"could not insert ${items.size}", Some(ex)))
        }
        ,
        _ => Right(items.size)
      )
    }

  }

  override def read(id: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Option[Country]]] = Future {
    withConnection { implicit connection: Connection =>
    Try {
      val q: SimpleSql[Row] = byIdQ(id)
      val result: Option[Country] = q.as(countryParser.singleOpt)
      result
    }.fold(
      ex => {
        logger.warn(s"count not read $id because $ex")
        Left(ItemNotFoundException(id))
      },
      result => Right(result)
    )
    }
  }(dbEx)

  override def delete(id: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Option[ID]]] = Future {
    withConnection { implicit connection: Connection =>
      Try {
        val q = deleteQ(id)
        q.executeUpdate()
      }.fold(
        ex => {
          logger.warn(s"could not delete $id, because $ex")
          Left(DeleteException(s"could not delete $id", Some(ex)))
        }
        ,
        rowCount => if (rowCount == 1) Right(Some(id))
        else Right(None)
      )
    }
    }(dbEx)

  override def update(item: Country)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Country]] = ???

}


object CountryDaoImpl {

  private val logger = Logger(getClass.getName)
  //
  // queries
  //
  private val byIdQ: ID => SimpleSql[Row] = id => SQL"select * from countries where id = ${id.value.toString}"

  private val insertInto = "insert into countries(id, country_name, country_abbreviation, create_date, version)"
  private def countryToValues(country: Country): String = {
   val result =  s"(${country.id.value.toString}, ${country.countryName.value}, ${country.countryAbbreviation.value}, ${country.createDate.asTimestamp}, ${country.version.value})"
    logger.info(s"country $country => $result")
    result

  }

  private val insertQ: Country => SimpleSql[Row] = country => SQL"""
          $insertInto
          values(${countryToValues(country)})
      """

  private val insertMultipleQ: Seq[Country] => SimpleSql[Row] = countries => {
    val values: Seq[String] = countries.map(c => countryToValues(c) + ",")
    val reversed = values.reverse
    val updated = reversed.head.dropRight(1) + "\n;\n"
    val valuesList = (updated +: reversed.tail).reverse.mkString("\n")
    SQL"""
          $insertInto
          values
          $valuesList
         """
  }

  private val deleteQ: ID => SimpleSql[Row] = id => SQL"delete from countries where id = ${id.value.toString}"

  //
  // parsers
  // if there needs to be parsers for a sub-set of country fields, it would also go here
  //

  private val countryParser: RowParser[Country] = {
    getID ~ getVersion ~ getCountyName ~ getCountryAbbreviation map {
      case id ~ version ~ countryName ~ countryAbbreviation =>
        Country(id, version, countryName, countryAbbreviation)
    }
  }

  private def getCountyName: RowParser[CountryName] = get[String](CountryName.fieldName).map(CountryName.build)
  private def getCountryAbbreviation: RowParser[CountryAbbreviation] =
    get[String](CountryAbbreviation.fieldName).map(CountryAbbreviation.build)
}
