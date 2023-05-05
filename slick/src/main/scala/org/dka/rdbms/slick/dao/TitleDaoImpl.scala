package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao.TitleDao
import org.dka.rdbms.common.model.components.{ID, Price, PublishDate, PublisherID, TitleName}
import org.dka.rdbms.common.model.item.Title
import org.dka.rdbms.common.model.item
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class TitleDaoImpl(override val db: Database) extends CrudDaoImpl[Title] with TitleDao {

  import TitleDaoImpl._
  //
  // crud IO operations
  //
  override protected val singleInsertIO: Title => DBIO[Int] = title => tableQuery += title
  override protected val multipleInsertIO: Seq[Title] => DBIO[Option[Int]] = titles => tableQuery ++= titles
  override protected val getIO: (ID, ExecutionContext) => DBIO[Option[Title]] = (id, ec) =>
    tableQuery.filter(_.id === id.value.toString).result.map(_.headOption)(ec)
  override protected val deletedIO: ID => DBIO[Int] = id => tableQuery.filter(_.id === id.value.toString).delete

  //
  // additional IO operations
  // needed to support AuthorDao
  //
}

object TitleDaoImpl {
  val tableQuery = TableQuery[TitleTable]

  class TitleTable(tag: Tag)
    extends Table[Title](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "titles") {
    val id = column[String]("id", O.PrimaryKey) // This is the primary key column
    private val title = column[String]("title")
    private val price = column[BigDecimal]("price")
    private val publisher = column[Option[String]]("publisher")
    private val publishedDate = column[Option[LocalDate]]("zip")

    // Every table needs a * projection with the same type as the table's type parameter
    override def * = (id, title, price, publisher, publishedDate) <> (fromDB, toDB)
  }

  //
  // conversions between db and model
  // the model is guaranteed valid,
  // the db is assumed valid because the data only come from the model
  //

  private type TitleTuple = (
    String, // id
    String, // name
    BigDecimal, // price
    Option[String], // publisherId
    Option[LocalDate] // published date
  )

  def fromDB(tuple: TitleTuple): Title = {
    val (id, name, price, publisherId, publishedDate) = tuple
    item.Title(
      ID.build(UUID.fromString(id)),
      name = TitleName.build(name),
      price = Price.build(price),
      publisher = publisherId.map(s => PublisherID.build(UUID.fromString(s))),
      publishedDate = publishedDate.map(d => PublishDate.build(d))
    )
  }

  def toDB(title: Title): Option[TitleTuple] = Some(
    title.id.value.toString,
    title.name.value,
    title.price.value,
    title.publisher.map(_.value.toString),
    title.publishedDate.map(_.value)
  )

}
