package org.dka.rdbms.db.load

import org.dka.rdbms.common.model.fields.{ID, LocationID, PublisherName, Version, WebSite}
import org.dka.rdbms.common.model.item.Publisher
import Generator._

import java.util.UUID

class PublisherGenerator(
  override val count: Int,
  val locationIds: Seq[UUID],
  override val fileName: String = "publisherInsert.sql")
  extends ItemGenerator {
  import PublisherGenerator._

  override val headerLine: String = bulkLoadPublisher.header

  override def insertLine(uuid: UUID): String = {
    val publisher = Publisher(
      ID(uuid),
      Version.defaultVersion,
      PublisherName.build(genString(PublisherName.maxLength)),
      Some(LocationID.build(randomLocationId)),
      Some(WebSite.build(genWebSite(WebSite.maxLength)))
    )
    bulkLoadPublisher.insertLine(publisher)
  }
  private val locationSize = locationIds.size
  private def randomLocationId: UUID = locationIds(util.Random.nextInt(locationSize))
}

object PublisherGenerator {
  private val bulkLoadPublisher: BulkLoad[Publisher] = new BulkLoad[Publisher] {
    override def header: String = "insert into publishers(id, publisher_name, location_id, website)\n  values\n"

    override def insertLine(p: Publisher): String = {
      val locationId = p.locationId.fold("'null'")(id => s"${id.value.toString}")
      val webSite = p.webSite.fold("'null'")(site => s"${site.value}")

      s"('${p.id.value.toString}', '${p.name.value}', '$locationId', '$webSite'),"
    }
  }
}
