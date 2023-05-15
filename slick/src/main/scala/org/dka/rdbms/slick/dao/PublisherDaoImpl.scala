package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao.PublisherDao
import org.dka.rdbms.common.model.fields.{ID, LocationID, PublisherName, Version, WebSite}
import org.dka.rdbms.common.model.item.Publisher
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.util.UUID
import scala.concurrent.ExecutionContext

class PublisherDaoImpl(override val db: Database) extends CrudDaoImpl[Publisher] with PublisherDao {
  import PublisherDaoImpl._

  //
  // crud IO operations
  //
  override protected val singleCreateIO: Publisher => DBIO[Int] = publisher => tableQuery += publisher
  override protected val multipleCreateIO: Seq[Publisher] => DBIO[Option[Int]] = publishers => tableQuery ++= publishers
  override protected val getIO: (ID, ExecutionContext) => DBIO[Option[Publisher]] = (id, ec) =>
    // the '_' is what comes back from the db, so _.id is a string based on the AuthorTable definition
    // the id is the model object, which is a final case class Id(...)
    tableQuery.filter(_.id === id.value.toString).result.map(_.headOption)(ec)
  override protected val deletedIO: ID => DBIO[Int] = id => tableQuery.filter(_.id === id.value.toString).delete

  //
  // additional IO operations
  // needed to support PublisherDao
  //

}

object PublisherDaoImpl {
  val tableQuery = TableQuery[PublisherTable]

  class PublisherTable(tag: Tag)
    extends Table[Publisher](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "publishers") {
    val id = column[String]("id", O.PrimaryKey) // This is the primary key column
    val version = column[Int]("version")
    private val publisherName = column[String]("publisher_name")
    private val locationId = column[Option[String]]("location_id")
    private val website = column[Option[String]]("website")

    override def * = (id, version, publisherName, locationId, website) <> (fromDB, toDB)
  }

  //
  // conversions between db and model
  // the model is guaranteed valid,
  // the db is assumed valid because the data only come from the model
  //
  private type PublisherTuple = (
    String, // id
    Int, // version
    String, // publisherName
    Option[String], // locationId
    Option[String] // website
  )

  def fromDB(tuple: PublisherTuple): Publisher = {
    val (id, version, publisherName, locationId, webSite) = tuple
    Publisher(
      ID.build(UUID.fromString(id)),
      Version.build(version),
      PublisherName.build(publisherName),
      LocationID.fromOpt(locationId),
      WebSite.build(webSite)
    )
  }

  def toDB(publisher: Publisher): Option[PublisherTuple] = Some(
    publisher.id.value.toString,
    publisher.version.value,
    publisher.name.value,
    publisher.locationId.map(_.value.toString),
    publisher.webSite.map(_.value)
  )
}
