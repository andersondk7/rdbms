package org.dka.rdbms.dao

import org.dka.rdbms.model.{Publisher, PublisherDao}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext

class PublisherDaoImpl(override val db: Database) extends CrudDaoImpl[Publisher, String](db) with PublisherDao {
  private val tableQuery = TableQuery[PublisherTable]
  override val singleInsertQuery: Publisher => DBIO[Int] = publisher => tableQuery += publisher
  override val multipleInsertQuery: Seq[Publisher] => DBIO[Option[Int]] = publishers => tableQuery ++= publishers
  override val getQuery: (String, ExecutionContext) => DBIO[Option[Publisher]] = (id, ec) =>
    tableQuery.filter(_.id === id).result.map(_.headOption)(ec)
  override val deletedQuery: String => DBIO[Int] = id => tableQuery.filter(_.id === id).delete

  private class PublisherTable(tag: Tag)
    extends Table[Publisher](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "publishers") {
    val id = column[String]("id", O.PrimaryKey) // This is the primary key column
    private val name = column[String]("name")
    private val address = column[String]("address")
    private val city = column[String]("city")
    private val state = column[String]("state")
    private val zip = column[String]("zip")

    // Every table needs a * projection with the same type as the table's type parameter
    override def * = (id, name, address, city, state, zip) <> (Publisher.tupled, Publisher.unapply)
  }
}
