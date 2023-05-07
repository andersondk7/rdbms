package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.model.components.ID
import org.dka.rdbms.common.model.item.AuthorTitleRelationship
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.util.UUID
import scala.language.implicitConversions

class AuthorsTitlesDao(val db: Database) {
//  import AuthorsTitlesDao._

}

object AuthorsTitlesDao {
  val tableQuery = TableQuery[AuthorsTitlesTable]

  class AuthorsTitlesTable(tag: Tag)
    extends Table[AuthorTitleRelationship](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "authors_titles") {
    val authorId = column[String]("author_id")
    val titleId = column[String]("title_id")
    val authorOrder = column[Int]("author_order")

    // Every table needs a * projection with the same type as the table's type parameter
    override def * = (authorId, titleId, authorOrder) <> (fromDB, toDB)
  }

  //
  // conversions between db and model
  // the model is guaranteed valid,
  // the db is assumed valid because the data only come from the model
  //

  private type AuthorTitleTuple = (
    String, // authorID
    String, // TitleId
    Int // authorOrder
  )

  def fromDB(tuple: AuthorTitleTuple): AuthorTitleRelationship = {
    val (authorId, titleId, authorOrder) = tuple
    AuthorTitleRelationship(
      authorId = ID.build(UUID.fromString(authorId)),
      titleId = ID.build(UUID.fromString(authorId)),
      authorOrder = authorOrder
    )
  }

  def toDB(relationship: AuthorTitleRelationship): Option[AuthorTitleTuple] = Some(
    relationship.authorId.value.toString,
    relationship.titleId.value.toString,
    relationship.authorOrder
  )

}
