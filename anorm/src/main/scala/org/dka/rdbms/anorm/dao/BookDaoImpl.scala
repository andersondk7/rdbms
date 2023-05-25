package org.dka.rdbms.anorm.dao

import anorm.*
import anorm.SqlParser.*
import com.typesafe.scalalogging.Logger
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

class BookDaoImpl(override val dataSource: HikariDataSource) extends CrudDaoImpl[Book] with BookDao {

  import BookDaoImpl.*

  override val tableName = "books"

  override protected def insertQ(book: Book): SimpleSql[Row] =
    SQL(
      " insert into books (id, version, title, price, publisher_id, publish_date, create_date) values ({id}, {version}, {title}, {price}, {publisher_id}, {publish_date}, {create_date})")
      .on(
        "id" -> book.id.value.toString,
        "version" -> book.version.value,
        "title" -> book.title.value,
        "price" -> book.price.value,
        "publisher_id" -> book.publisherID.map(_.value.toString).orNull,
        "publish_date" -> book.publishDate.map(_.value).orNull,
        "create_date" -> book.createDate.asTimestamp
      )

  override protected def updateQ(book: Book): SimpleSql[Row] = {
    val publisherId = book.publisherID.map(_.value.toString).orNull
    val publishDate = book.publishDate.map(_.value).orNull

    SQL"""
          update books
           set
             version = ${book.version.value},
             title = ${book.title.value},
             price = ${book.price.value},
             publisher_id = ${publisherId},
             publish_date = ${publishDate},
             update_date = ${book.lastUpdate.get.asTimeStamp}
          where id = ${book.id.value.toString}
   """
  }

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

  def getAllIds(implicit ec: ExecutionContext): Future[DaoErrorsOr[Seq[ID]]] = ???

  def getAuthorsForBook(bookId: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Seq[BookAuthorSummary]]] = ???

}

object BookDaoImpl {

  //
  // queries specific to AuthorDao
  //

  //
  // parsers
  // parsers for fields that are not unique to Author are in the package object
  // if there needs to be parsers for a sub-set of Author fields, it would also go here
  //

  private def getTitle: RowParser[Title] = get[String](Title.fieldName).map(Title.build)
  private def getPrice: RowParser[Price] = get[BigDecimal](Price.fieldName).map(Price.build)
  private def getPublishDate: RowParser[Option[PublishDate]] =
    get[Option[LocalDate]](PublishDate.fieldName).map(PublishDate.fromOpt)
  private def getPublisherId: RowParser[Option[PublisherID]] =
    get[Option[String]](PublisherID.fieldName).map(PublisherID.fromOpt)

}
