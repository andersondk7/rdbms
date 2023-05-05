package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao.AuthorDao
import org.dka.rdbms.common.model.components.{FirstName, ID, LastName, LocationID}
import org.dka.rdbms.common.model.item
import org.dka.rdbms.common.model.item.Author
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class AuthorDaoImpl(override val db: Database) extends CrudDaoImpl[Author] with AuthorDao {
  import AuthorDaoImpl._

  //
  // crud IO operations
  //
  override protected val singleInsertIO: Author => DBIO[Int] = author => tableQuery += author
  override protected val multipleInsertIO: Seq[Author] => DBIO[Option[Int]] = authors => tableQuery ++= authors
  override protected val getIO: (ID, ExecutionContext) => DBIO[Option[Author]] = (id, ec) =>
    // the '_' is what comes back from the db, so _.id is a string based on the AuthorTable definition
    // the id is the model object, which is a final case class Id(...)
    tableQuery.filter(_.id === id.value.toString).result.map(_.headOption)(ec)
  override protected val deletedIO: ID => DBIO[Int] = id => tableQuery.filter(_.id === id.value.toString).delete

  //
  // additional IO operations
  // needed to support AuthorDao
  //
}

object AuthorDaoImpl {
  val tableQuery = TableQuery[AuthorTable]

  class AuthorTable(tag: Tag)
    extends Table[Author](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "authors") {
    val id = column[String]("id", O.PrimaryKey) // This is the primary key column
    private val lastName = column[String]("last_name")
    private val firstName = column[Option[String]]("first_name")
    private val locationId = column[Option[String]]("location_id")

    // Every table needs a * projection with the same type as the table's type parameter
    override def * = (id, lastName, firstName, locationId) <> (fromDB, toDB)
  }

  //
  // conversions between db and model
  // the model is guaranteed valid,
  // the db is assumed valid because the data only come from the model
  //

  private type AuthorTuple = (
    String, // id
    String, // last name
    Option[String], // first name
    Option[String] // location id
  )

  def fromDB(tuple: AuthorTuple): Author = {
    val (id, lastName, firstName, locationId) = tuple
    item.Author(
      ID.build(UUID.fromString(id)),
      lastName = LastName.build(lastName),
      firstName = firstName.map(FirstName.build),
      locationId = locationId.map { s =>
        println(s"building locationId from $s")
        LocationID.build(UUID.fromString(s))
      }
    )

  }

  def toDB(author: Author): Option[AuthorTuple] = Some(
    author.id.value.toString,
    author.lastName.value,
    author.firstName.map(_.value),
    author.locationId.map(_.value.toString)
  )

}
