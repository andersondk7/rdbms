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

class AuthorDaoImpl(override val dataSource: HikariDataSource, dbEx: ExecutionContext) extends CrudDaoImpl[Author] with AuthorDao {

  import AuthorDaoImpl.*

  override val tableName = "authors"

  //
  // queries
  //
  override protected def insertQ(author: Author): SimpleSql[Row] =
    SQL(
      """ insert into authors (id, version, last_name, first_name, location_id, create_date)
          values ({id}, {version}, {lastName}, {firstName}, {locationId}, {createDate})"""
    )
      .on(
        "id"          -> author.id.value.toString,
        "version"     -> author.version.value,
        "lastName"   -> author.lastName.value,
        "firstName"  -> author.firstName.map(_.value).orNull,
        "locationId" -> author.locationId.map(_.value.toString).orNull,
        "createDate" -> author.createDate.asTimestamp
      )

  override protected def updateQ(author: Author): SimpleSql[Row] =
    SQL("""
          update authors
           set
             version = {version},
             last_name = {lastName},
             first_name = {firstName},
             location_id = {locationId},
             update_date = {lastUpdate}
          where id = {id}
   """)
      .on(
        "version"    -> author.version.value,
        "lastName"   -> author.lastName.value,
        "firstName"  -> author.firstName.map(_.value).orNull,
        "locationId" -> author.locationId.map(_.value.toString).orNull,
        "lastUpdate" -> author.lastUpdate.map(_.asTimeStamp).orNull,
        "id"         -> author.id.value.toString
      )

  //
  // parsers
  //
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

  def getLastName: RowParser[LastName] = get[String](LastName.fieldName).map(LastName.build)

  def getFirstName: RowParser[Option[FirstName]] =
    get[Option[String]](FirstName.fieldName).map(FirstName.fromOpt)

}
