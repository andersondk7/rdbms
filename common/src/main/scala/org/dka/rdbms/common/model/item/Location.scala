package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.validation.Validation._
import org.dka.rdbms.common.model.fields.{CountryID, ID, LocationAbbreviation, LocationName, Version}

final case class Location(
  override val id: ID,
  override val version: Version,
  locationName: LocationName,
  locationAbbreviation: LocationAbbreviation,
  countryID: CountryID)
  extends Updatable[Location] {
  override def update: Location = this.copy(version = version.next)
}

object Location {
  implicit val encodeLocation: Encoder[Location] = (l: Location) => {
    val objects = List(
      ID.toJson(l.id),
      Version.toJson(l.version),
      LocationName.toJson(l.locationName),
      LocationAbbreviation.toJson(l.locationAbbreviation),
      CountryID.toJson(l.countryID)
    )
    Json.obj(objects: _*)
  }

  implicit val decodeAuthor: Decoder[Location] = (c: HCursor) => {
    val result: ValidationErrorsOr[Location] =
      (
        ID.fromJson(c),
        Version.fromJson(c),
        LocationName.fromJson(c),
        LocationAbbreviation.fromJson(c),
        CountryID.fromJson(c)
      ).mapN(Location.apply)
    result match {
      case Invalid(errors) =>
        Left(DecodingFailure(errors, Nil))
      case Valid(location) => Right(location)
    }
  }
}
