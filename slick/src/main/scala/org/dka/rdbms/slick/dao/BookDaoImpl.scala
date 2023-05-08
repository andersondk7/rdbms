package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao.BookDao
import org.dka.rdbms.common.model.components.{ID, Price, PublishDate, PublisherID, Title}
import org.dka.rdbms.common.model.item.Book
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class BookDaoImpl(override val db: Database) extends CrudDaoImpl[Book] with BookDao {

  import BookDaoImpl._
  //
  // crud IO operations
  //
  override protected val singleInsertIO: Book => DBIO[Int] = title => tableQuery += title
  override protected val multipleInsertIO: Seq[Book] => DBIO[Option[Int]] = titles => tableQuery ++= titles
  override protected val getIO: (ID, ExecutionContext) => DBIO[Option[Book]] = (id, ec) =>
    tableQuery.filter(_.id === id.value.toString).result.map(_.headOption)(ec)
  override protected val deletedIO: ID => DBIO[Int] = id => tableQuery.filter(_.id === id.value.toString).delete

  //
  // additional IO operations
  // needed to support AuthorDao
  //
}

object BookDaoImpl {
  val tableQuery = TableQuery[BooksTable]

  class BooksTable(tag: Tag)
    extends Table[Book](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "books") {
    val id = column[String]("id", O.PrimaryKey) // This is the primary key column
    val title = column[String]("title")
    val price = column[BigDecimal]("price")
    val publisherId = column[Option[String]]("publisher_id")
    val publishDate = column[Option[LocalDate]]("publish_date")

    // Every table needs a * projection with the same type as the table's type parameter
    override def * = (id, title, price, publisherId, publishDate) <> (fromDB, toDB)
  }

  //
  // conversions between db and model
  // the model is guaranteed valid,
  // the db is assumed valid because the data only come from the model
  //

  private type BookTuple = (
    String, // id
    String, // title
    BigDecimal, // price
    Option[String], // publisherId
    Option[LocalDate] // published date
  )

  def fromDB(tuple: BookTuple): Book = {
    val (id, title, price, publisherId, publishDate) = tuple
    Book(
      ID.build(UUID.fromString(id)),
      title = Title.build(title),
      price = Price.build(price),
      publisherID = publisherId.map(s => PublisherID.build(UUID.fromString(s))),
      publishDate = publishDate.map(d => PublishDate.build(d))
    )
  }

  def toDB(book: Book): Option[BookTuple] = Some(
    book.id.value.toString,
    book.title.value,
    book.price.value,
    book.publisherID.map(_.value.toString),
    book.publishDate.map(_.value)
  )

}
