package org.dka.rdbms.anorm.dao

import anorm.*
import anorm.SqlParser.*
import com.typesafe.scalalogging.Logger
import com.zaxxer.hikari.HikariDataSource
import org.dka.rdbms.common.dao.*
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.*
import org.dka.rdbms.common.model.item.Publisher
import org.dka.rdbms.anorm.dao.*

import java.sql.Connection
import java.util.UUID
import scala.util.Try
import scala.concurrent.{ExecutionContext, Future}

class PublisherDaoImpl(override val dataSource: HikariDataSource) extends CrudDaoImpl[Publisher] with PublisherDao {

  import PublisherDaoImpl.*

  override val tableName = "publishers"

  override protected def insertQ(publisher: Publisher): SimpleSql[Row] =
    SQL(
      " insert into publishers (id, version, publisher_name, location_id, website, create_date) values ({id}, {version}, {publisher_name}, {location_id}, {website}, {create_date})")
      .on(
        "id" -> publisher.id.value.toString,
        "version" -> publisher.version.value,
        "publisher_name" -> publisher.publisherName.value,
        "location_id" -> publisher.locationId.map(_.value.toString).orNull,
        "website" -> publisher.webSite.map(_.value).orNull,
        "create_date" -> publisher.createDate.asTimestamp
      )

  override protected def updateQ(publisher: Publisher): SimpleSql[Row] = {
    val locationId = publisher.locationId.map(_.value.toString).orNull
    val website = publisher.webSite.map(_.value).orNull

    SQL"""
          update publishers
           set version = ${publisher.version.value},
           publisher_name = ${publisher.publisherName.value},
           location_id = ${locationId},
           website = ${website},
           update_date = ${publisher.lastUpdate.get.asTimeStamp}
          where id = ${publisher.id.value.toString}
   """
  }

  override protected val itemParser: RowParser[Publisher] =
    getID ~ getVersion ~ getPublisherName ~ getLocationId ~ getWebsite ~ getCreateDate ~ getUpdateDate map {
      case id ~ v ~ pn ~ pl ~ pw ~ cd ~ up =>
        Publisher(
          id = id,
          version = v,
          publisherName = pn,
          locationId = pl,
          webSite = pw,
          createDate = cd,
          lastUpdate = up
        )
    }
}

object PublisherDaoImpl {

  //
  // queries specific to PublisherDao
  //

  //
  // parsers
  // parsers for fields that are not unique to Publisher are in the package object
  // if there needs to be parsers for a sub-set of publisher fields, it would also go here
  //

  private def getPublisherName: RowParser[PublisherName] = get[String](PublisherName.fieldName).map(PublisherName.build)
  private def getLocationId: RowParser[Option[LocationID]] =
    get[Option[String]](LocationID.fieldName).map(LocationID.fromOpt)
  private def getWebsite: RowParser[Option[WebSite]] = get[Option[String]](WebSite.fieldName).map(WebSite.fromOpt)
}
