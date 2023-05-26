package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.validation.Validation._
import org.dka.rdbms.common.model.fields.{CountryAbbreviation, CountryName, CreateDate, ID, UpdateDate, Version}

final case class Country(
  override val id: ID,
  override val version: Version,
  countryName: CountryName,
  countryAbbreviation: CountryAbbreviation,
  createDate: CreateDate = CreateDate.now,
  override val lastUpdate: Option[UpdateDate] = None)
  extends Updatable[Country] {

  override def update: Country = this.copy(version = version.next, lastUpdate = UpdateDate.now)

}

object Country {

  implicit val encodeAuthor: Encoder[Country] = (c: Country) => {
    val objects = List(
      Some(ID.toJson(c.id)),
      Some(Version.toJson(c.version)),
      UpdateDate.toJson(c.lastUpdate),
      Some(CountryName.toJson(c.countryName)),
      Some(CountryAbbreviation.toJson(c.countryAbbreviation)),
      Some(CreateDate.toJson(c.createDate))
    ).flatten
    Json.obj(objects: _*)
  }

  implicit val decodeAuthor: Decoder[Country] = (c: HCursor) => {
    val result: ValidationErrorsOr[Country] =
      (
        ID.fromJson(c),
        Version.fromJson(c),
        CountryName.fromJson(c),
        CountryAbbreviation.fromJson(c),
        CreateDate.fromJson(c),
        UpdateDate.fromOptionalJson(c)
      ).mapN(Country.apply)
    result match {
      case Invalid(errors) =>
        Left(DecodingFailure(errors, Nil))
      case Valid(country) => Right(country)
    }
  }

}
