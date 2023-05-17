package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao.LocationDao
import org.dka.rdbms.common.model.fields.{CountryID, ID, LocationAbbreviation, LocationName, Version}
import org.dka.rdbms.common.model.item.Location
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class LocationDaoImpl(override val db: Database) extends CrudDaoImpl[Location] with LocationDao {
  import LocationDaoImpl._

  //
  // crud IO operations
  //
  override protected val singleCreateIO: Location => DBIO[Int] = location => tableQuery += location
  override protected val multipleCreateIO: Seq[Location] => DBIO[Option[Int]] = locations => tableQuery ++= locations
  override protected val getIO: (ID, ExecutionContext) => DBIO[Option[Location]] = (id, ec) =>
    tableQuery.filter(_.id === id.value.toString).result.map(_.headOption)(ec)
  override protected val deletedIO: ID => DBIO[Int] = id => tableQuery.filter(_.id === id.value.toString).delete

  override protected val updateAction: (Location, ExecutionContext) => DBIO[Location] = (item, ec) => {
    val updated = item.update
    tableQuery
      .filter(_.id === item.id.value.toString)
      .map(lt =>
        (
          lt.id,
          lt.version,
          lt.locationName,
          lt.locationAbbreviation,
          lt.countryID
        ))
      .update(
        (
          updated.id.value.toString,
          updated.version.value,
          updated.locationName.value,
          updated.locationAbbreviation.value,
          updated.countryID.value.toString
        ))
      .map(_ => updated)(ec) // convert number of rows updated to the updated item (i.e. updated version etc.)
  }

  //
  // additional IO operations
  // needed to support LocationDao
  //
}

object LocationDaoImpl {
  val tableQuery = TableQuery[LocationTable]

  class LocationTable(tag: Tag)
    extends Table[Location](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "locations") {
    val id = column[String]("id", O.PrimaryKey) // This is the primary key column
    val version = column[Int]("version")
    val locationName = column[String]("location_name")
    val locationAbbreviation = column[String]("location_abbreviation")
    val countryID = column[String]("country_id")

    // Every table needs a * projection with the same type as the table's type parameter
    override def * = (id, version, locationName, locationAbbreviation, countryID) <> (fromDB, toDB)
  }

  //
  // conversions between db and model
  // the model is guaranteed valid,
  // the db is assumed valid because the data only come from the model
  //

  private type LocationTuple = (
    String, // id
    Int, // version
    String, // location_name
    String, // location_abbreviation
    String // country_id
  )

  def fromDB(tuple: LocationTuple): Location = {
    val (id, version, locationName, locationAbbreviation, countryID) = tuple
    Location(
      ID.build(UUID.fromString(id)),
      Version.build(version),
      locationName = LocationName.build(locationName),
      locationAbbreviation = LocationAbbreviation.build(locationAbbreviation),
      countryID = CountryID.build(UUID.fromString(countryID))
    )
  }

  def toDB(location: Location): Option[LocationTuple] = Some(
    location.id.value.toString,
    location.version.value,
    location.locationName.value,
    location.locationAbbreviation.value,
    location.countryID.value.toString
  )

}
