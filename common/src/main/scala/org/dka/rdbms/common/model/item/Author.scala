package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.fields.{CountryID, FirstName, ID, LastName, LocationID}
import org.dka.rdbms.common.model.validation.Validation._

final case class Author(
  id: ID,
  lastName: LastName,
  firstName: Option[FirstName],
  locationId: Option[LocationID]) {}

object Author {
  implicit val encodeAuthor: Encoder[Author] = (a: Author) => {
    val objects = List(
      Some(ID.toJson(a.id)),
      Some(LastName.toJson(a.lastName)),
      FirstName.toJson(a.firstName),
      LocationID.toJson(a.locationId)
    ).flatten // filter out the None, i.e. only needed lines
    Json.obj(objects: _*)
  }

  implicit val decodeAuthor: Decoder[Author] = (c: HCursor) => {
    val result: ValidationErrorsOr[Author] =
      (
        ID.fromJson(c),
        LastName.fromJson(c),
        FirstName.fromOptionalJson(c),
        LocationID.fromOptionalJson(c)
      ).mapN(Author.apply)
    result match {
      case Invalid(errors) =>
        Left(DecodingFailure(errors, Nil))
      case Valid(author) => Right(author)
    }
  }
}
