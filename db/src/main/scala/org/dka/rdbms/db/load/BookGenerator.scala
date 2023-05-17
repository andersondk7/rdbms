package org.dka.rdbms.db.load

import org.dka.rdbms.common.model.fields.{ID, Price, PublishDate, PublisherID, Title, Version}
import org.dka.rdbms.common.model.item.Book
import Generator._

import java.util.UUID

class BookGenerator(
  override val count: Int,
  val publisherIds: Seq[UUID],
  override val fileName: String = "bookInsert.sql")
  extends ItemGenerator {

  import BookGenerator._

  override val headerLine: String = bulkLoadBook.header

  override def insertLine(uuid: UUID): String = {
    val book = Book(
      ID(uuid),
      Version.defaultVersion,
      Title.build(genString(Title.maxLength)),
      Price.build(genPrice),
      Some(PublisherID.build(randomPublisherID)),
      Some(PublishDate.build(genDate))
    )
    bulkLoadBook.insertLine(book)
  }
  private val publisherSize = publisherIds.size
  private def randomPublisherID: UUID = publisherIds(util.Random.nextInt(publisherSize))
}

object BookGenerator {
  private val bulkLoadBook: BulkLoad[Book] = new BulkLoad[Book] {
    override def header: String = "insert into books(id, title, price, publisher_id, publish_date)\n  values\n"

    override def insertLine(b: Book): String = {
      val publisherId = b.publisherID.fold("'null'")(id => s"${id.value.toString}")
      val publisherDate = b.publishDate.fold("'null'")(date => s"${date.value.toString}")

      s"('${b.id.value.toString}', '${b.title.value}', '${b.price.value.toString}', '$publisherId', '$publisherDate'),"
    }
  }
}
