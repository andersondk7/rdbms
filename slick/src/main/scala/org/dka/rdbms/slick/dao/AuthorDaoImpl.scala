package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao.AuthorDao
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.components.{FirstName, ID, LastName, LocationID, Title}
import org.dka.rdbms.common.model.item
import org.dka.rdbms.common.model.item.{Author, AuthorBookRelationship}
import org.dka.rdbms.common.model.query.BookAuthorSummary
import org.dka.rdbms.slick.dao
import org.dka.rdbms.slick.dao.AuthorsBooksDao.AuthorsBooksTable
import shapeless.tupled
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

class AuthorDaoImpl(override val db: Database) extends CrudDaoImpl[Author] with AuthorDao {

  import AuthorDaoImpl._

  //
  // crud IO operations
  //
  override protected val singleInsertIO: Author => DBIO[Int] = author => tableQuery += author
  override protected val multipleInsertIO: Seq[Author] => DBIO[Option[Int]] = authors => tableQuery ++= authors
  override protected val getIO: (ID, ExecutionContext) => DBIO[Option[Author]] = (id, ec) =>
    tableQuery.filter(x => x.id === id.value.toString).result.map(x => x.headOption)(ec)
  override protected val deletedIO: ID => DBIO[Int] = id => tableQuery.filter(_.id === id.value.toString).delete

  //
  // additional IO operations
  // needed to support AuthorDao
  //

  private val bookAuthorSummary: (ID, ExecutionContext) => DBIO[Seq[BookAuthorSummary]] = (bookId, ec) => {
    // the first join:  join author_books table and books table on bookId  -> (authorBookTable, bookTable)
    // second join:  join the first with authors table on authorBookTable.authorID == authorTable.id

    val innerJoin = for {
      ((authorBookTable, bookTable), authorTable) <-
        AuthorsBooksDao.tableQuery join
          BookDaoImpl.tableQuery on (_.bookId === _.id) join
          AuthorDaoImpl.tableQuery on (_._1.authorId === _.id)
    } yield ((authorBookTable, bookTable), authorTable)
    innerJoin
      // authorBookTable.bookId == bookTable.bookId
      .filter(_._1._1.bookId === bookId.value.toString)
      .result
      .map(seq =>
        seq.map { result =>
          val relationship: AuthorBookRelationship = result._1._1
          val book = result._1._2 // from bookTable
          val author = result._2 // from authorTable
          BookAuthorSummary(relationship, book, author)
        })(ec)
  }

  //
  // implementation of AuthorDao methods
  //
  def getAuthorsForBook(bookId: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Seq[BookAuthorSummary]]] =
    db.run(bookAuthorSummary(bookId, ec)).map(r => Right(r))

}

object AuthorDaoImpl {
  val tableQuery = TableQuery[AuthorTable]

  class AuthorTable(tag: Tag)
    extends Table[Author](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "authors") {
    val id = column[String]("id", O.PrimaryKey) // This is the primary key column
    val lastName = column[String]("last_name")
    val firstName = column[Option[String]]("first_name")
    val locationId = column[Option[String]]("location_id")

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
