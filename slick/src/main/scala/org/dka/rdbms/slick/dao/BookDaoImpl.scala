package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao.BookDao
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.{ID, Price, PublishDate, PublisherID, Title}
import org.dka.rdbms.common.model.item.{AuthorBookRelationship, Book}
import org.dka.rdbms.common.model.query.BookAuthorSummary
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
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

  val getAllIdsIO: (ExecutionContext) => DBIO[Seq[ID]] = (ec) =>
    tableQuery.result.map(seq => seq.map(at => at.id))(ec)

  private val bookAuthorSummaryIO: (ID, ExecutionContext) => DBIO[Seq[BookAuthorSummary]] = (bookId, ec) => {
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
  // implementation of BookDao methods
  //

  override def getAllIds(implicit ec: ExecutionContext): Future[DaoErrorsOr[Seq[ID]]] =
    db.run(getAllIdsIO(ec))
      .map(r => Right(r))

  override def getAuthorsForBook(bookId: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Seq[BookAuthorSummary]]] =
    db.run(bookAuthorSummaryIO(bookId, ec)).map(r => Right(r))

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
