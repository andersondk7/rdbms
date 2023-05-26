package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.validation.Validation._
import org.dka.rdbms.common.model.fields.{CountryID, CreateDate, ID, LocationAbbreviation, LocationName, UpdateDate, Version}

final case class Location(
  override val id: ID,
  override val version: Version,
  locationName: LocationName,
  locationAbbreviation: LocationAbbreviation,
  countryID: CountryID,
  createDate: CreateDate = CreateDate.now,
  override val lastUpdate: Option[UpdateDate] = None)
  extends Updatable[Location] {

  override def update: Location = this.copy(version = version.next, lastUpdate = UpdateDate.now)

}

object Location {

  implicit val encodeLocation: Encoder[Location] = (l: Location) => {
    val objects = List(
      Some(ID.toJson(l.id)),
      Some(Version.toJson(l.version)),
      UpdateDate.toJson(l.lastUpdate),
      Some(LocationName.toJson(l.locationName)),
      Some(LocationAbbreviation.toJson(l.locationAbbreviation)),
      Some(CountryID.toJson(l.countryID)),
      Some(CreateDate.toJson(l.createDate))
    ).flatten
    Json.obj(objects: _*)
  }

  implicit val decodeAuthor: Decoder[Location] = (c: HCursor) => {
    val result: ValidationErrorsOr[Location] =
      (
        ID.fromJson(c),
        Version.fromJson(c),
        LocationName.fromJson(c),
        LocationAbbreviation.fromJson(c),
        CountryID.fromJson(c),
        CreateDate.fromJson(c),
        UpdateDate.fromOptionalJson(c)
      ).mapN(Location.apply)
    result match {
      case Invalid(errors) =>
        Left(DecodingFailure(errors, Nil))
      case Valid(location) => Right(location)
    }
  }

}
