package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao.CountryDao
import org.dka.rdbms.common.model.fields.{CountryAbbreviation, CountryName, ID, Version}
import org.dka.rdbms.common.model.item.Country
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class CountryDaoImpl(override val db: Database) extends CrudDaoImpl[Country] with CountryDao {
  import CountryDaoImpl._

  //
  // crud IO operations
  //
  override protected val singleCreateIO: Country => DBIO[Int] = country => tableQuery += country
  override protected val multipleCreateIO: Seq[Country] => DBIO[Option[Int]] = countries => tableQuery ++= countries
  override protected val getIO: (ID, ExecutionContext) => DBIO[Option[Country]] = (id, ec) =>
    tableQuery.filter(_.id === id.value.toString).result.map(_.headOption)(ec)
  override protected val deletedIO: ID => DBIO[Int] = id => tableQuery.filter(_.id === id.value.toString).delete

  //
  // additional IO operations
  // needed to support AuthorDao
  //
}

object CountryDaoImpl {
  val tableQuery = TableQuery[CountryTable]

  class CountryTable(tag: Tag)
    extends Table[Country](
      tag,
      None, // schema is set at connection time rather than a compile time, see DBConfig notes
      "countries") {
    val id = column[String]("id", O.PrimaryKey) // This is the primary key column
    val version = column[Int]("version")
    private val countryName = column[String]("country_name")
    private val countryAbbreviation = column[String]("country_abbreviation")

    // Every table needs a * projection with the same type as the table's type parameter
    override def * = (id, version, countryName, countryAbbreviation) <> (fromDB, toDB)
  }

  //
  // conversions between db and model
  // the model is guaranteed valid,
  // the db is assumed valid because the data only come from the model
  //

  private type CountryTuple = (
    String, // id
    Int, // version
    String, // country_name
    String // country_abbreviation
  )

  def fromDB(tuple: CountryTuple): Country = {
    val (id, version, countryName, countryAbbreviation) = tuple
    Country(
      ID.build(UUID.fromString(id)),
      Version.build(version),
      countryName = CountryName.build(countryName),
      countryAbbreviation = CountryAbbreviation.build(countryAbbreviation)
    )
  }

  def toDB(country: Country): Option[CountryTuple] = Some(
    country.id.value.toString,
    country.version.value,
    country.countryName.value,
    country.countryAbbreviation.value
  )

}
