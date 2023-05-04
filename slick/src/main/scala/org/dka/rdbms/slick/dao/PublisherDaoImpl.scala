package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao.PublisherDao
import org.dka.rdbms.common.model.components.{Address, City, CompanyName, ID, State, Zip}
import org.dka.rdbms.common.model.item.Publisher
import org.dka.rdbms.common.model.item
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import PublisherDaoImpl._
import java.util.UUID
import scala.concurrent.ExecutionContext

class PublisherDaoImpl(override val db: Database) extends CrudDaoImpl[Publisher] with PublisherDao {

  //
  // crud IO operations
  //
  override val singleInsertIO: Publisher => DBIO[Int] = publisher => tableQuery += publisher
  override val multipleInsertIO: Seq[Publisher] => DBIO[Option[Int]] = publishers => tableQuery ++= publishers
  override val getIO: (ID, ExecutionContext) => DBIO[Option[Publisher]] = (id, ec) =>
    // the '_' is what comes back from the db, so _.id is a string based on the AuthorTable definition
    // the id is the model object, which is a final case class Id(...)
    tableQuery.filter(_.id === id.value.toString).result.map(_.headOption)(ec)
  override val deletedIO: ID => DBIO[Int] = id => tableQuery.filter(_.id === id.value.toString).delete

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
    private val name = column[String]("name")
    private val address = column[Option[String]]("address")
    private val city = column[Option[String]]("city")
    private val state = column[Option[String]]("state")
    private val zip = column[Option[String]]("zip")

    override def * = (id, name, address, city, state, zip) <> (fromDB, toDB)
  }

  //
  // conversions between db and model
  // the model is guaranteed valid,
  // the db is assumed valid because the data only come from the model
  //
  private type PublisherTuple = (
    String, // id
    String, // name
    Option[String], // address
    Option[String], // city
    Option[String], // state
    Option[String] // zip
  )

  def fromDB(tuple: PublisherTuple): Publisher = {
    val (id, name, address, city, state, zip) = tuple
    item.Publisher(
      ID.build(UUID.fromString(id)),
      CompanyName.build(name),
      Address.build(address),
      City.build(city),
      State.build(state),
      Zip.build(zip)
    )
  }

  def toDB(publisher: Publisher): Option[PublisherTuple] = Some(
    publisher.id.value.toString,
    publisher.name.value,
    publisher.address.map(_.value),
    publisher.city.map(_.value),
    publisher.state.map(_.value),
    publisher.zip.map(_.value)
  )
}
