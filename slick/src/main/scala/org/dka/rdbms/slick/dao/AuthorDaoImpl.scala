package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao.AuthorDao
import org.dka.rdbms.common.model.{Address, Author, City, FirstName, ID, LastName, Phone, State, Zip}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class AuthorDaoImpl(override val db: Database) extends CrudDaoImpl[Author, ID] with AuthorDao {
  private val tableQuery = TableQuery[AuthorTable]
  override val singleInsertQuery: Author => DBIO[Int] = author => tableQuery += author
  override val multipleInsertQuery: Seq[Author] => DBIO[Option[Int]] = authors => tableQuery ++= authors
  override val getQuery: (ID, ExecutionContext) => DBIO[Option[Author]] = (id, ec) =>
    // the '_' is what comes back from the db, so _.id is a string based on the AuthorTable definition
    // the id is the model object, which is a final case class Id(...)
    tableQuery.filter(_.id === id.value).result.map(_.headOption)(ec)
  override val deletedQuery: ID => DBIO[Int] = id => tableQuery.filter(_.id === id.value).delete
  private class AuthorTable(tag: Tag)
    extends Table[Author](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "authors") {
    val id = column[String]("au_id", O.PrimaryKey) // This is the primary key column
    private val au_lname = column[String]("au_lname")
    private val au_fname = column[String]("au_fname")
    private val phone = column[Option[String]]("phone")
    private val address = column[Option[String]]("address")
    private val city = column[Option[String]]("city")
    private val state = column[Option[String]]("state")
    private val zip = column[Option[String]]("zip")

    import AuthorDaoImpl._
    // Every table needs a * projection with the same type as the table's type parameter
    override def * = (id, au_lname, au_fname, phone, address, city, state, zip) <> (fromDB, toDB)
  }
}

object AuthorDaoImpl {

  //
  // conversions between db and model
  // the model is guaranteed valid,
  // the db is assumed valid because the data only come from the model
  //

  private type AuthorTuple = (
    String, // id
    String, // last name
    String, // first name
    Option[String], // phone
    Option[String], // address
    Option[String], // city
    Option[String], // state
    Option[String] // zip
  )

  def fromDB(tuple: AuthorTuple): Author = {
    val (id, lastName, firstName, phone, address, city, state, zip) = tuple
    Author(
      ID.build(id),
      lastName = LastName.build(lastName),
      firstName = FirstName.build(firstName),
      phone = phone.map(Phone.build),
      address = address.map(Address.build),
      city = city.map(City.build),
      state = state.map(State.build),
      zip = zip.map(Zip.build)
    )
  }

  def toDB(author: Author): Option[AuthorTuple] = Some(
    author.id.value,
    author.lastName.value,
    author.firstName.value,
    author.phone.map(_.value),
    author.address.map(_.value),
    author.city.map(_.value),
    author.state.map(_.value),
    author.zip.map(_.value)
  )

}
