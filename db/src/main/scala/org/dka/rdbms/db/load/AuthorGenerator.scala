package org.dka.rdbms.db.load

import org.dka.rdbms.common.model.fields.{FirstName, ID, LastName, LocationID, Version}
import org.dka.rdbms.common.model.item.Author
import Generator._

import java.util.UUID

class AuthorGenerator(
  override val count: Int,
  val locationIds: Seq[UUID],
  override val fileName: String = "authorInsert.sql")
  extends ItemGenerator {
  import AuthorGenerator._

  override val headerLine: String = bulkLoadAuthor.header

  override def insertLine(uuid: UUID): String = {
    val author = Author(
      ID(uuid),
      Version.defaultVersion,
      LastName.build(genString(LastName.maxLength)),
      Some(FirstName.build(genString(FirstName.maxLength))),
      Some(LocationID.build(randomLocationId))
    )
    bulkLoadAuthor.insertLine(author)
  }
  private val locationSize = locationIds.size
  private def randomLocationId: UUID = locationIds(util.Random.nextInt(locationSize))
}

object AuthorGenerator {
  private val bulkLoadAuthor: BulkLoad[Author] = new BulkLoad[Author] {
    override def header: String = "insert into authors(id, last_name, first_name, location_id)\n  values\n"

    override def insertLine(a: Author): String = {
      val locationId = a.locationId.fold("'null'")(id => s"${id.value.toString}")
      val firstName = a.firstName.fold("'null'")(name => s"${name.value}")

      s"('${a.id.value.toString}', '${a.lastName.value}', '$firstName', '$locationId'),"
    }
  }
}
