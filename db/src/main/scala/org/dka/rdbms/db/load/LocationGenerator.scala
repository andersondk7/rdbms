package org.dka.rdbms.db.load

import org.dka.rdbms.common.model.fields.{CountryID, ID, LocationAbbreviation, LocationName}
import org.dka.rdbms.common.model.item.Location

import java.util.UUID
import scala.util.Random

class LocationGenerator(
                        override val count: Int,
                        countryIds: Seq[UUID],
                        override val fileName: String = "locationInsert.sql"
                       ) extends ItemGenerator {
  import LocationGenerator._
  override val headerLine: String = bulkLoadLocation.header

  override def insertLine(uuid: UUID):String = {

    val location = Location(
      ID(uuid),
      LocationName.build("locationName"),
      LocationAbbreviation.build("LA"),
      CountryID.build(randomCountryId)
    )
    bulkLoadLocation.insertLine(location)
  }
  private val countrySize = countryIds.length
  private def randomCountryId: UUID = countryIds(random.nextInt(countrySize))
}


object LocationGenerator {
  private val random = new Random
  private val bulkLoadLocation: BulkLoad[Location] = new BulkLoad[Location] {
    override def header: String =
      "insert into local.locations (id, location_name, location_abbreviation, country_id)\n  values\n"

    override def insertLine(l: Location): String = s"('${l.id.value.toString}', '${l.locationName.value}', '${l.locationAbbreviation.value}', '${l.countryID.value.toString}'),"
  }
}
