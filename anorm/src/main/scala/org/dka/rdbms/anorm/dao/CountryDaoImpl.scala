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

class CountryDaoImpl(override val dataSource: HikariDataSource) extends CrudDaoImpl[Country] with CountryDao {

  import CountryDaoImpl.*

  override protected def byIdQ(id: ID): SimpleSql[Row] = SQL"select * from countries where id = ${id.value.toString}"

  override protected def insertQ(country: Country): SimpleSql[Row] = {
    SQL(
      "insert into countries (id, country_name, country_abbreviation, create_date, version) values ({id}, {country_name}, {country_abbreviation}, {create_date}, {version})")
      .on(
        "id" -> country.id.value.toString,
        "country_name" -> country.countryName.value,
        "country_abbreviation" -> country.countryAbbreviation.value,
        "create_date" -> country.createDate.asTimestamp,
        "version" -> country.version.value
      )
  }

  override protected def deleteQ(id: ID): SimpleSql[Row] = SQL"delete from countries where id = ${id.value.toString}"

  override protected def updateQ(country: Country): SimpleSql[Row] = {
    SQL"""
    update countries
    set version = ${country.version.value},
       country_name = ${country.countryName.value},
       country_abbreviation = ${country.countryAbbreviation.value},
       update_date = ${country.lastUpdate.get.asTimeStamp}
    where id = ${country.id.value.toString}
    """
  }

  override protected val itemParser:RowParser[Country] =
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

  private def getCountyName: RowParser[CountryName] = get[String](CountryName.fieldName).map(CountryName.build)
  private def getCountryAbbreviation: RowParser[CountryAbbreviation] =
    get[String](CountryAbbreviation.fieldName).map(CountryAbbreviation.build)
}
