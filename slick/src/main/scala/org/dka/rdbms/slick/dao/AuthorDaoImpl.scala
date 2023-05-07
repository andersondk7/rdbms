package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao.AuthorDao
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.components.{FirstName, ID, LastName, LocationID, TitleName}
import org.dka.rdbms.common.model.item
import org.dka.rdbms.common.model.item.Author
import org.dka.rdbms.common.model.query.TitleAuthorSummary
import org.dka.rdbms.slick.dao
import shapeless.tupled
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

class AuthorDaoImpl(override val db: Database) extends CrudDaoImpl[Author] with AuthorDao {
  import AuthorDaoImpl._

  //
  // crud IO operations
  //
  override protected val singleInsertIO: Author => DBIO[Int] = author => tableQuery += author
  override protected val multipleInsertIO: Seq[Author] => DBIO[Option[Int]] = authors => tableQuery ++= authors
  override protected val getIO: (ID, ExecutionContext) => DBIO[Option[Author]] = (id, ec) =>
    tableQuery.filter( x => x.id === id.value.toString).result.map(x => x.headOption)(ec)
  override protected val deletedIO: ID => DBIO[Int] = id => tableQuery.filter(_.id === id.value.toString).delete

  //
  // additional IO operations
  // needed to support AuthorDao
  //
//  private val authorIdTitleName: (ID, ExecutionContext) => DBIO[Seq[(ID, TitleName)]] = {
//  (titleId, ec) =>
//    authorsForTitleQuery.filter(x => x._1.titleId  === titleId.value.toString)
//      .result.map(x => x.map(y => {
//      val (authorTitle, title) =  y
//      (authorTitle.authorId, title.name)
//    })
//    )(ec)
//  }

  /**
   * returns (AuthorsTitleTable, TitlesTable)
   */
//  private val authorsForTitleQuery = for {
//    (at, t)  <- AuthorsTitlesDao.tableQuery.join(TitleDaoImpl.tableQuery) on (_.titleId === _.id)
//  } yield {
//    (at, t)
//    }

  private val titleAuthorSummaryQuery = for {
    authorTitleT <- AuthorsTitlesDao.tableQuery
    titleT <- BookDaoImpl.tableQuery //if (authorTitleT.titleId === titleT.id)
//    authorT <- tableQuery.filter( x => x.id === "0d38fea9-90ae-498f-b142-5c5f645c54d5")
  } yield {
    (authorTitleT.titleId, authorTitleT.authorId, titleT.title)
  }

  /**
   * returns Seq[work, author's lastName, author's firstName
   */
//  private val titleAuthorSummaryQuery2 = for {
//    ( (_, titleT), authorT) <- authorsForTitleQuery.join(AuthorDaoImpl.tableQuery) on (_._1.authorId === _.id)
//  } yield {
//    (titleT.title, authorT.lastName, authorT.firstName )
//  }

  private val titleAuthorSummaryIO1: (ID, ExecutionContext) => DBIO[Seq[TitleAuthorSummary]] =
  (titleId, ec) =>
    DBIO.successful(
      List(TitleAuthorSummary.apply("the TitleName", "author's LastName", Some(" author's FirstName")))
    )


  private val titleAuthorSummaryIO2: (ID, ExecutionContext) => DBIO[Seq[TitleAuthorSummary]] = (titleId, ec) => {
    val authorTableQuery = AuthorsTitlesDao.tableQuery//.filter(_.titleId === titleId.value.toString )
    val titleTableQuery = for {
      (atT, titleT) <- authorTableQuery.join(BookDaoImpl.tableQuery).on(_.titleId === _.id)
  } yield (atT, titleT)
    val summaries = titleTableQuery.filter(x => x._1.titleId === titleId.value.toString)
      .result.map(_.map(seq  => TitleAuthorSummary(seq._2.title.value, "authorLastName", Some("authorFirstName")))) (ec)

    DBIO.successful(List(TitleAuthorSummary("title", "authorLastName", Some("authorFirstName"))))
    }


  private val titleAuthorSummaryIO3: (ID, ExecutionContext) => DBIO[Seq[TitleAuthorSummary]] = (titleId, ec) => {
    val innerJoin = for {
      (atT, titleT) <- AuthorsTitlesDao.tableQuery join BookDaoImpl.tableQuery on (_.titleId === _.id)
    } yield (titleT)
    innerJoin.filter(_.id === titleId.value.toString)
      .result
      .map(seq => seq.map(title => TitleAuthorSummary(title.title.value, "authorLastName", Some("authorFirstName"))))(ec)
  }

  //
  // implementation of AuthorDao methods
  //
  def getAuthorsForTitle(titleId: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Seq[TitleAuthorSummary]]] = {
    db.run(titleAuthorSummaryIO3(titleId, ec)).map(r => {
      Right(r)
    })
  }

}

object AuthorDaoImpl {
  val tableQuery = TableQuery[AuthorTable]

  class AuthorTable(tag: Tag)
    extends Table[Author](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "authors") {
    val id = column[String]("id", O.PrimaryKey) // This is the primary key column
    val lastName = column[String]("last_name")
    val firstName = column[Option[String]]("first_name")
    val locationId = column[Option[String]]("location_id")

    // Every table needs a * projection with the same type as the table's type parameter
    override def * = (id, lastName, firstName, locationId) <> (fromDB, toDB)
  }

  //
  // conversions between db and model
  // the model is guaranteed valid,
  // the db is assumed valid because the data only come from the model
  //

  private type AuthorTuple = (
    String, // id
    String, // last name
    Option[String], // first name
    Option[String] // location id
  )

  def fromDB(tuple: AuthorTuple): Author = {
    val (id, lastName, firstName, locationId) = tuple
    item.Author(
      ID.build(UUID.fromString(id)),
      lastName = LastName.build(lastName),
      firstName = firstName.map(FirstName.build),
      locationId = locationId.map { s =>
        println(s"building locationId from $s")
        LocationID.build(UUID.fromString(s))
      }
    )

  }

  def toDB(author: Author): Option[AuthorTuple] = Some(
    author.id.value.toString,
    author.lastName.value,
    author.firstName.map(_.value),
    author.locationId.map(_.value.toString)
  )

}
