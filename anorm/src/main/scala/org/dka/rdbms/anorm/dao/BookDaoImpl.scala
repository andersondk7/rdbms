package org.dka.rdbms.anorm.dao

import anorm.*
import anorm.SqlParser.*
import com.zaxxer.hikari.HikariDataSource
import org.dka.rdbms.common.dao.*
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.*
import org.dka.rdbms.common.model.item.Book
import org.dka.rdbms.anorm.dao.*
import org.dka.rdbms.common.model.query.BookAuthorSummary

import java.time.LocalDate
import java.sql.Connection
import java.util.UUID
import scala.util.Try
import scala.concurrent.{ExecutionContext, Future}

class BookDaoImpl(override val dataSource: HikariDataSource, override val dbEx: ExecutionContext)
  extends CrudDaoImpl[Book]
    with BookDao {

  import BookDaoImpl.*

  override val tableName = "books"

  //
  // queries
  //
  override protected def insertQ(book: Book): SimpleSql[Row] =
    SQL("""
      insert into books (id, version, title, price, publisher_id, publish_date, create_date)
      values ({id}, {version}, {title}, {price}, {publisher_id}, {publish_date}, {create_date})
     """)
      .on(
        "id"           -> book.id.value.toString,
        "version"      -> book.version.value,
        "title"        -> book.title.value,
        "price"        -> book.price.value,
        "publisher_id" -> book.publisherID.map(_.value.toString).orNull,
        "publish_date" -> book.publishDate.map(_.value).orNull,
        "create_date"  -> book.createDate.asTimestamp
      )

  override protected def updateQ(book: Book): SimpleSql[Row] =
    SQL("""
          update books
           set
             version = {version},
             title = {title},
             price = {price},
             publisher_id = {publisherId},
             publish_date = {publishDate},
             update_Date = {lastUpdate}
          where id = {id}
   """)
      .on(
        "version"     -> book.version.value,
        "title"       -> book.title.value,
        "price"       -> book.price.value,
        "publisherId" -> book.publisherID.map(_.value.toString).orNull,
        "publishDate" -> book.publishDate.map(_.value).orNull,
        "lastUpdate"  -> book.lastUpdate.map(_.value).orNull,
        "id"          -> book.id.value.toString
      )

  //
  // parsers
  //
  override protected val itemParser: RowParser[Book] =
    getID ~ getVersion ~ getTitle ~ getPrice ~ getPublisherId ~ getPublishDate ~ getCreateDate ~ getUpdateDate map {
      case id ~ v ~ t ~ p ~ pid ~ pd ~ cd ~ up =>
        Book(
          id = id,
          version = v,
          title = t,
          price = p,
          publisherID = pid,
          publishDate = pd,
          createDate = cd,
          lastUpdate = up
        )
    }

  //
  // BookDao methods
  //
  override def getAllIds(implicit ec: ExecutionContext): Future[DaoErrorsOr[Seq[ID]]] =
    withConnection(dbEx) { implicit connection: Connection =>
      Try {
        allIdsQ.as(getID.*)
      }.fold(
        ex => Left(QueryException(s"could not getAllIds", Some(ex))),
        records => Right(records)
      )
    }

  override def getBookAuthorSummary(
    bookId: ID
  )(implicit ec: ExecutionContext
  ): Future[DaoErrorsOr[Seq[BookAuthorSummary]]] =
    withConnection(dbEx) { implicit connection: Connection =>
      Try {
        bookAuthorSummaryQ(bookId).as(authorBookSummaryParser.*)
      }.fold(
        ex => Left(QueryException(s"could not bookAuthorSummary for book $bookId", Some(ex))),
        summaries => Right(summaries)
      )
    }

}

object BookDaoImpl {

  //
  // queries specific to BookDao
  //
  private val allIdsQ =
    SQL("select * from books")

  private def bookAuthorSummaryQ(bookId: ID) = SQL("""
    select b.title, a.last_name, a.first_name, r.author_order
    from authors_books as r
    join books as b on b .id = r.book_id
    join authors as a on a .id = r.author_id
    where r.book_id = {bookId}
    """)
    .on("bookId" -> bookId.value.toString)

  //
  // parsers
  // parsers for fields that are not unique to Author are in the package object
  // if there needs to be parsers for a sub-set of Author fields, it would also go here
  //

  def getTitle: RowParser[Title] = get[String](Title.fieldName).map(Title.build)

  def getPrice: RowParser[Price] = get[BigDecimal](Price.fieldName).map(Price.build)

  def getPublishDate: RowParser[Option[PublishDate]] =
    get[Option[LocalDate]](PublishDate.fieldName).map(PublishDate.fromOpt)

  def getPublisherId: RowParser[Option[PublisherID]] =
    get[Option[String]](PublisherID.fieldName).map(PublisherID.fromOpt)

  def getAuthorOrder: RowParser[Int] = get[Int]("author_order")

  def authorBookSummaryParser: RowParser[BookAuthorSummary] =
    getTitle ~ AuthorDaoImpl.getLastName ~ AuthorDaoImpl.getFirstName ~ getAuthorOrder map { case t ~ ln ~ fn ~ ao =>
      BookAuthorSummary(
        titleName = t,
        authorLastName = ln,
        authorFirstName = fn,
        authorOrder = ao
      )
    }

}
