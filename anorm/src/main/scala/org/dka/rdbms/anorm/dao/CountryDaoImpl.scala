package org.dka.rdbms.anorm.dao

import anorm.*
import anorm.SqlParser.*
import com.typesafe.scalalogging.Logger
import com.zaxxer.hikari.HikariDataSource
import org.dka.rdbms.common.dao.*
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.{CountryAbbreviation, CountryName, ID, Version}
import org.dka.rdbms.common.model.item.Country
import org.dka.rdbms.anorm.dao.*

import java.sql.Connection
import java.util.UUID
import scala.util.Try
import scala.concurrent.{ExecutionContext, Future}

class CountryDaoImpl(override val dataSource: HikariDataSource, override val dbEx: ExecutionContext)
  extends CrudDaoImpl[Country]
    with CountryDao {

  import CountryDaoImpl.*

  override val tableName: String = "countries"

  //
  // queries
  //
  override protected def insertQ(country: Country): SimpleSql[Row] =
    SQL("""
      insert into countries (id, country_name, country_abbreviation, create_date, version)
      values ({id}, {countryName}, {countryAbbreviation}, {createDate}, {version})
     """)
      .on(
        "id"                  -> country.id.value.toString,
        "countryName"         -> country.countryName.value,
        "countryAbbreviation" -> country.countryAbbreviation.value,
        "createDate"          -> country.createDate.asTimestamp,
        "version"             -> country.version.value
      )

  override protected def updateQ(country: Country): SimpleSql[Row] =
    SQL("""
    update countries
    set
       version = {version},
       country_name = {countryName},
       country_abbreviation = {countryAbbreviation},
       update_date = {lastUpdate}
    where id = {id}
    """)
      .on(
        "version"             -> country.version.value,
        "countryName"         -> country.countryName.value,
        "countryAbbreviation" -> country.countryAbbreviation.value,
        "lastUpdate"          -> country.lastUpdate.map(_.value).orNull,
        "id"                  -> country.id.value.toString
      )

  //
  // parsers
  //
  override protected val itemParser: RowParser[Country] =
    getID ~ getVersion ~ getCountyName ~ getCountryAbbreviation ~ getCreateDate ~ getUpdateDate map {
      case id ~ v ~ cn ~ ca ~ cd ~ up =>
        Country(
          id = id,
          version = v,
          countryName = cn,
          countryAbbreviation = ca,
          createDate = cd,
          lastUpdate = up
        )
    }

  //
  // CountryDao methods
  //

}

object CountryDaoImpl {

  //
  // queries specific to CountryDao
  //

  //
  // parsers
  // parsers for fields that are not unique to Country are in the package object
  // if there needs to be parsers for a sub-set of country fields, it would also go here
  //

  def getCountyName: RowParser[CountryName] = get[String](CountryName.fieldName).map(CountryName.build)

  def getCountryAbbreviation: RowParser[CountryAbbreviation] =
    get[String](CountryAbbreviation.fieldName).map(CountryAbbreviation.build)

}
