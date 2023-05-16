package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.validation.Validation._
import org.dka.rdbms.common.model.fields.{CountryAbbreviation, CountryName, ID, Version}

final case class Country(
  override val id: ID,
  override val version: Version,
  countryName: CountryName,
  countryAbbreviation: CountryAbbreviation
                        ) extends Updatable[Country] {
  override def update: Country = this.copy(version = version.next)
}

object Country {
  implicit val encodeAuthor: Encoder[Country] = (c: Country) => {
    val objects = List(
      ID.toJson(c.id),
      Version.toJson(c.version),
      CountryName.toJson(c.countryName),
      CountryAbbreviation.toJson(c.countryAbbreviation)
    )
    Json.obj(objects: _*)
  }

  implicit val decodeAuthor: Decoder[Country] = (c: HCursor) => {
    val result: ValidationErrorsOr[Country] =
      (
        ID.fromJson(c),
        Version.fromJson(c),
        CountryName.fromJson(c),
        CountryAbbreviation.fromJson(c)
      ).mapN(Country.apply)
    result match {
      case Invalid(errors) =>
        Left(DecodingFailure(errors, Nil))
      case Valid(country) => Right(country)
    }
  }
}
