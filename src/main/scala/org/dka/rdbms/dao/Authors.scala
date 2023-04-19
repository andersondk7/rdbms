package org.dka.rdbms.dao

import org.dka.rdbms.model.{Author, AuthorDao, DaoException, DeleteException, InsertException, QueryException}
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

// todo: consider separating DBIO from the actual running ...
class Authors(db: Database) extends AuthorDao {
  private val tableQuery = TableQuery[AuthorTable]

  override def insertAuthor(author: Author)(implicit ec: ExecutionContext): Future[Either[DaoException, Author]] = {
    val insertQuery: DBIO[Int] = tableQuery += author
    db.run(insertQuery).map(c => {
      if (c == 1) Right(author)
      else Left(InsertException(s"could not insert $author"))
    })
  }

  override def insertAuthor(authors: Seq[Author])(implicit ec: ExecutionContext): Future[Either[DaoException, Int]] = {
    val insertQuery: DBIO[Option[Int]] = tableQuery ++= authors
    db.run(insertQuery).map {
      case Some(count) => Right(count)
      case None => Left(InsertException(s"coula.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.nio=Ad not insert ${authors.size} into authors"))
    }
  }

  override def getAuthor(id: String)(implicit ec: ExecutionContext): Future[Either[DaoException, Option[Author]]] = {
    val query: DBIO[Option[Author]] = tableQuery.filter(_.id === id).result.map(_.headOption)
    // note this only gets the first, assume that since id is the primary key, there will only be one!
    db.run(query).map (r => {
      Right(r)

    })  // don't know how to capture when it fails...
  }

  override def deleteAuthor(id: String)(implicit ec: ExecutionContext): Future[Either[DaoException, Option[String]]] = {
    val query: DBIO[Int] = tableQuery.filter(_.id === id).delete
    db.run(query).map {
      case 0 => Right(None)
      case _ => Right(Some(id)) // again assumes that since id is the primary key, there will only be one
    }
  }

  class AuthorTable(tag: Tag)
    extends Table[Author](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "authors") {
    val id = column[String]("au_id", O.PrimaryKey) // This is the primary key column
    val au_lname = column[String]("au_lname")
    val au_fname = column[String]("au_fname")
    val phone = column[String]("phone")
    val address = column[String]("address")
    val city = column[String]("city")
    val state = column[String]("state")
    val zip = column[String]("zip")
    // Every table needs a * projection with the same type as the table's type parameter
    override def * = (id, au_lname, au_fname, phone, address, city, state, zip) <> (Author.tupled, Author.unapply)
  }

}

