package org.dka.rdbms.anorm.dao


import anorm.*
import anorm.SqlParser.*
import com.typesafe.scalalogging.Logger
import com.zaxxer.hikari.HikariDataSource
import org.dka.rdbms.common.dao.*
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.{CountryID, ID, LocationAbbreviation, LocationName, Version}
import org.dka.rdbms.common.model.item.Location
import org.dka.rdbms.anorm.dao.*

import java.sql.Connection
import java.util.UUID
import scala.util.Try
import scala.concurrent.{ExecutionContext, Future}

class LocationDaoImpl(override val dataSource: HikariDataSource) extends CrudDaoImpl [Location]  with LocationDao {

  import LocationDaoImpl.*

  override protected def insertQ(location: Location): SimpleSql[Row] =
    SQL(
      "insert into locations(id, version, location_name, location_abbreviation, country_id, create_date) values ({id}, {version}, {location_name}, {location_abbreviation}, {country_id}, {create_date})"
    ).on(
      "id" -> location.id.value.toString,
      "version" -> location.version.value,
      "location_name" -> location.locationName.value,
      "location_abbreviation" -> location.locationAbbreviation.value,
      "country_id" -> location.countryID.value.toString,
      "create_date" -> location.createDate.asTimestamp
    )

  override protected def byIdQ(id: ID): SimpleSql[Row] = SQL"select * from locations where id = ${id.value.toString}"

  override protected def deleteQ(id: ID): SimpleSql[Row] = SQL"delete from locations where id = ${id.value.toString}"

  override protected def updateQ(location: Location): SimpleSql[Row] = {
    SQL"""
  update locations
  set version = ${location.version.value},
     location_name = ${location.locationName.value},
     location_abbreviation = ${location.locationAbbreviation.value},
     country_id = ${location.countryID.value.toString},
     update_date = ${location.lastUpdate.get.asTimeStamp}
  where id = ${location.id.value.toString}
  """
  }

    override protected def itemParser: RowParser[Location] =
      getID ~ getVersion ~ getLocationName ~ getLocationAbbreviation ~ getCountryId ~ getCreateDate ~ getUpdateDate map {
        case id ~ v ~ ln ~ la ~ ci ~ cd ~ ud =>
          Location(
            id = id,
            version = v,
            locationName = ln,
            locationAbbreviation = la,
            countryID = ci,
            createDate = cd,
            lastUpdate = ud
          )
      }
}

object LocationDaoImpl {
  private def getLocationName: RowParser[LocationName] = get[String](LocationName.fieldName).map(LocationName.build)
  private def getLocationAbbreviation: RowParser[LocationAbbreviation] = get[String](LocationAbbreviation.fieldName).map(LocationAbbreviation.build)
  private def getCountryId: RowParser[CountryID] = get[String](CountryID.fieldName).map(CountryID.build)
}