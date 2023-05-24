package org.dka.rdbms.anorm.dao

import anorm.*
import anorm.SqlParser.*
import com.typesafe.scalalogging.Logger
import com.zaxxer.hikari.HikariDataSource
import org.dka.rdbms.common.dao.{CountryDao, CrudDao, DeleteException, InsertException, InvalidVersionException, ItemNotFoundException, UpdateException}
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.{CountryAbbreviation, CountryName, ID, Version}
import org.dka.rdbms.common.model.item.Country
import org.dka.rdbms.anorm.dao.*

import java.sql.Connection
import java.util.UUID
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
        },
        _ => Right(item)
      )
    }
  }

  override def create(items: Seq[Country])(implicit ec: ExecutionContext): Future[DaoErrorsOr[Int]] = Future {
    withConnection { _ =>
      Try {
        items.map(create)
      }.fold(
        ex => {
          logger.warn(s"could not insert ${items.size}, because $ex")
          Left(InsertException(s"could not insert ${items.size}", Some(ex)))
        },
        booleans =>
          if (booleans.contains(false)) Left(InsertException(s"could not insert all ${items.size}"))
          else Right(items.size)
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
      )}
  }

  override def delete(id: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Option[ID]]] = Future {
    withConnection { implicit connection: Connection =>
      Try {
        val q = deleteQ(id)
        q.executeUpdate()
      }.fold(
        ex => {
          logger.warn(s"could not delete $id, because $ex")
          Left(DeleteException(s"could not delete $id", Some(ex)))
        },
        rowCount =>
          if (rowCount == 1) Right(Some(id))
          else Right(None)
      )
    }
  }(dbEx)

  override def update(item: Country)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Country]] = {
    implicit val connection: Connection = dataSource.getConnection
    connection.setAutoCommit(false)
    // these type of transaction boundary does not block until the row is released, it blows up
    // only these levels work:
    // Transaction_Repeatable_Read
    // Transaction_Serializable
    connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ)
    for {
      targetVersion <- checkVersion(item)
      result <- targetVersion match {
        case Left(ex) => Future.successful(Left(ex))
        case Right(_) => doUpdate(item)
      }
    } yield {
      connection.commit()
      connection.close()
      result
    }
  }

  private def checkVersion(item: Country)(implicit connection:Connection, ec: ExecutionContext): Future[DaoErrorsOr[Country]] = Future {
    Try {
      val q: SimpleSql[Row] = byIdQ(item.id)
      val result: Option[Country] = q.as(countryParser.singleOpt)
      result
    }.fold(
      ex => {
        logger.warn(s"count not read ${item.id} because $ex")
        Left(ItemNotFoundException(item.id))
      },
      {
        case None => Left(ItemNotFoundException(item.id))
        case Some(existing) => if (existing.version == item.version) Right(item)
        else Left(InvalidVersionException(existing.version, Some(item.version)))
      }
    )
  }

  private def doUpdate(item: Country)(implicit ec: ExecutionContext, connection: Connection): Future[DaoErrorsOr[Country]] = Future {
    val updated = item.update
    Try {
      val q = updateQ(updated)
      q.executeUpdate()
    }.fold(
      ex => {
        Left(UpdateException(updated.id, Some(ex)))
      },
      count => {
        if (count == 1) Right(updated)
        else Left(UpdateException(updated.id))
      }
    )
  }
}


object CountryDaoImpl {

  //
  // queries
  //
  private val byIdQ: ID => SimpleSql[Row] = id => SQL"select * from countries where id = ${id.value.toString}"

  private val insertQ: Country => SimpleSql[Row] = country =>
    SQL(
      "insert into countries (id, country_name, country_abbreviation, create_date, version) values ({id}, {country_name}, {country_abbreviation}, {create_date}, {version})")
      .on(
        "id" -> country.id.value.toString,
        "country_name" -> country.countryName.value,
        "country_abbreviation" -> country.countryAbbreviation.value,
        "create_date" -> country.createDate.asTimestamp,
        "version" -> country.version.value
      )

  private val deleteQ: ID => SimpleSql[Row] = id => SQL"delete from countries where id = ${id.value.toString}"

  private val updateQ: Country => SimpleSql[Row] = country =>
    SQL"""
      update countries
      set version = ${country.version.value},
         country_name = ${country.countryName.value},
         country_abbreviation = ${country.countryAbbreviation.value},
         update_date = ${country.lastUpdate.get.asTimeStamp}
      where id = ${country.id.value.toString}
      """
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
