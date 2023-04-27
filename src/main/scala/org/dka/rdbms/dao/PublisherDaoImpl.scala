package org.dka.rdbms.dao

import org.dka.rdbms.model._
import org.dka.rdbms.model.dao.PublisherDao
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext

class PublisherDaoImpl(override val db: Database) extends CrudDaoImpl[Publisher, ID] with PublisherDao {
  private val tableQuery = TableQuery[PublisherTable]
  override val singleInsertQuery: Publisher => DBIO[Int] = publisher => tableQuery += publisher
  override val multipleInsertQuery: Seq[Publisher] => DBIO[Option[Int]] = publishers => tableQuery ++= publishers
  override val getQuery: (ID, ExecutionContext) => DBIO[Option[Publisher]] = (id, ec) =>
    // the '_' is what comes back from the db, so _.id is a string based on the AuthorTable definition
    // the id is the model object, which is a final case class Id(...)
    tableQuery.filter(_.id === id.value).result.map(_.headOption)(ec)
  override val deletedQuery: ID => DBIO[Int] = id => tableQuery.filter(_.id === id.value).delete

  private class PublisherTable(tag: Tag)
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

    import PublisherDaoImpl._
    override def * = (id, name, address, city, state, zip) <> (fromDB, toDB)
  }
}

object PublisherDaoImpl {

  //
  // conversions between db and model
  // the model is type safe, the db is not
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
    Publisher(
      ID(id),
      CompanyName(name),
      Address(address),
      City(city),
      State(state),
      Zip(zip)
    )
  }

  def toDB(publisher: Publisher): Option[PublisherTuple] = Some(
    publisher.id.value,
    publisher.name.value,
    publisher.address.map(_.value),
    publisher.city.map(_.value),
    publisher.state.map(_.value),
    publisher.zip.map(_.value)
  )
}
