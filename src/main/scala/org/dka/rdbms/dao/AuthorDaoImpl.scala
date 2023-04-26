package org.dka.rdbms.dao

import org.dka.rdbms.model._
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class AuthorDaoImpl(override val db: Database) extends CrudDaoImpl[Author, ID](db) with AuthorDao {
  private val tableQuery = TableQuery[AuthorTable]
  override val singleInsertQuery: Author => DBIO[Int] = author => tableQuery += author
  override val multipleInsertQuery: Seq[Author] => DBIO[Option[Int]] = authors => tableQuery ++= authors
  override val getQuery: (ID, ExecutionContext) => DBIO[Option[Author]] = (id, ec) =>
    // the '_' is what comes back from the db, so _.id is a string based on the AuthorTable definition
    // the id is the model object, which is a final case class Id(...)
    tableQuery.filter(_.id  === id.value).result.map(_.headOption)(ec)
  override val deletedQuery: ID => DBIO[Int] = id => tableQuery.filter(_.id === id.value).delete
  private class AuthorTable(tag: Tag)
    extends Table[Author](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "authors") {
    val id = column[String]("au_id", O.PrimaryKey) // This is the primary key column
    private val au_lname = column[String]("au_lname")
    private val au_fname = column[String]("au_fname")
    private val phone = column[String]("phone")
    private val address = column[String]("address")
    private val city = column[String]("city")
    private val state = column[String]("state")
    private val zip = column[String]("zip")

    import AuthorDaoImpl._
    // Every table needs a * projection with the same type as the table's type parameter
    override def * = (id, au_lname, au_fname, phone, address, city, state, zip) <> (fromDB, toDB)
  }
}

object AuthorDaoImpl {

  //
  // conversions between db and model
  // the model is type safe, the db is not
  //
  private type AuthorTuple = (String, String, String, String, String, String, String, String)

  def fromDB(tuple: AuthorTuple): Author = {
    val (id, lastName, firstName, phone, address, city, state, zip) = tuple
    Author(
      ID(id),
      lastName = LastName(lastName),
      firstName = FirstName(firstName),
      phone = Phone(phone),
      address = Address(address),
      city = City(city),
      state = State(state),
      zip = Zip(zip)
    )
  }

  def toDB(author: Author): Option[AuthorTuple] = Some(
    author.id.value,
    author.lastName.value,
    author.firstName.value,
    author.phone.value,
    author.address.value,
    author.city.value,
    author.state.value,
    author.zip.value
  )


}
