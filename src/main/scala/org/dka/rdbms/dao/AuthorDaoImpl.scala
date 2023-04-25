package org.dka.rdbms.dao

import org.dka.rdbms.model.{Author, AuthorDao}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext

class AuthorDaoImpl(override val db: Database) extends CrudDaoImpl[Author, String](db) with AuthorDao {
  private val tableQuery = TableQuery[AuthorDaoImpl.AuthorTable]
  override val singleInsertQuery: Author => DBIO[Int] = author => tableQuery += author
  override val multipleInsertQuery: Seq[Author] => DBIO[Option[Int]] = authors => tableQuery ++= authors
  override val getQuery: (String, ExecutionContext) => DBIO[Option[Author]] = (id, ec) =>
    tableQuery.filter(_.id === id).result.map(_.headOption)(ec)
  override val deletedQuery: String => DBIO[Int] = id => tableQuery.filter(_.id === id).delete
}

object AuthorDaoImpl {

  private class AuthorTable(tag: Tag)
    extends Table[Author](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "authors") {
    val id = column[String]("au_id", O.PrimaryKey) // This is the primary key column
    private val au_lname = column[String]("au_lname")
    private val au_fname = column[String]("au_fname")
    private val phone = column[String]("phone")
    private val address = column[String]("address")
    private val city = column[String]("city")
    private val state = column[String]("state")
    private val zip = column[String]("zip")
    // Every table needs a * projection with the same type as the table's type parameter
    override def * = (id, au_lname, au_fname, phone, address, city, state, zip) <> (Author.tupled, Author.unapply)
  }

}
