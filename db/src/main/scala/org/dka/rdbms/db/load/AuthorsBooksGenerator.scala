package org.dka.rdbms.db.load

import org.dka.rdbms.common.model.fields.ID
import org.dka.rdbms.common.model.item.{AuthorBookRelationship, Book}

import java.util.UUID
import java.io._
import scala.util.Try

class AuthorsBooksGenerator(
  val authorIds: Seq[UUID],
  val bookIds: Seq[UUID],
  val fileName: String = "authorBookInsert.sql") {
  def write(): Try[Unit] =
    for {
      writer <- Try(new BufferedWriter(new FileWriter(new File(fileName))))
      _ <- Try(writer.write(headerLine))
      _ <- Try(writer.write(relationshipLines))
    } yield writer.close()

  private def relationshipLines: String = {

    val lines = bookIds
      .flatMap(bookId => randomAuthors(bookId))
      .map(r => s"('${r.bookId.value}', '${r.authorId.value}', '${r.authorOrder}'),")
    ItemGenerator.replaceTrailingComma(lines)
  }

  private val headerLine: String = "insert into authors_books (book_id, author_id, author_order)\n  values\n"
  private def randomAuthors(bookId: UUID): Seq[AuthorBookRelationship] = Range
    .inclusive(1, 4)
    .map(i => AuthorBookRelationship(ID.build(randomAuthorId), ID.build(bookId), i))

  private val authorIdsSize = authorIds.size
  private def randomAuthorId: UUID = authorIds(util.Random.nextInt(authorIdsSize))
}
