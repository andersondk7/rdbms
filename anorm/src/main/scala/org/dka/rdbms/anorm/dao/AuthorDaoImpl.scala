package org.dka.rdbms.anorm.dao

import anorm.*
import anorm.SqlParser.*
import com.typesafe.scalalogging.Logger
import com.zaxxer.hikari.HikariDataSource
import org.dka.rdbms.common.dao.*
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.*
import org.dka.rdbms.common.model.item.Author
import org.dka.rdbms.anorm.dao.*

import java.sql.Connection
import java.util.UUID
import scala.util.Try
import scala.concurrent.{ExecutionContext, Future}

class AuthorDaoImpl(override val dataSource: HikariDataSource) extends CrudDaoImpl[Author] with AuthorDao {

  import AuthorDaoImpl.*

  override val tableName = "authors"

  override protected def insertQ(author: Author): SimpleSql[Row] =
    SQL(
      " insert into authors (id, version, last_name, first_name, location_id, create_date) values ({id}, {version}, {last_name}, {first_name}, {location_id}, {create_date})")
      .on(
        "id" -> author.id.value.toString,
        "version" -> author.version.value,
        "last_name" -> author.lastName.value,
        "first_name" -> author.firstName.map(_.value).orNull,
        "location_id" -> author.locationId.map(_.value.toString).orNull,
        "create_date" -> author.createDate.asTimestamp
      )

  override protected def updateQ(author: Author): SimpleSql[Row] = {
    val firstName = author.firstName.map(_.value).orNull
    val locationId = author.locationId.map(_.value.toString).orNull

    SQL"""
          update authors
           set
             version = ${author.version.value},
             last_name = ${author.lastName.value},
             first_name = ${firstName},
             location_id = ${locationId},
             update_date = ${author.lastUpdate.get.asTimeStamp}
          where id = ${author.id.value.toString}
   """
  }

  override protected val itemParser: RowParser[Author] =
    getID ~ getVersion ~ getLastName ~ getFirstName ~ getLocationId ~ getCreateDate ~ getUpdateDate map {
      case id ~ v ~ ln ~ fn ~ lid ~ cd ~ up =>
        Author(
          id = id,
          version = v,
          lastName = ln,
          firstName = fn,
          locationId = lid,
          createDate = cd,
          lastUpdate = up
        )
    }
}

object AuthorDaoImpl {

  //
  // queries specific to AuthorDao
  //

  //
  // parsers
  // parsers for fields that are not unique to Author are in the package object
  // if there needs to be parsers for a sub-set of Author fields, it would also go here
  //

  private def getLastName: RowParser[LastName] = get[String](LastName.fieldName).map(LastName.build)
  private def getFirstName: RowParser[Option[FirstName]] =
    get[Option[String]](FirstName.fieldName).map(FirstName.fromOpt)

}
