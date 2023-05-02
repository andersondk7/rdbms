package org.dka.rdbms.common.model

import cats.implicits._
import cats.data.Validated._
import io.circe._
import Validation._

final case class Author(
  id: ID,
  lastName: LastName,
  firstName: FirstName,
  phone: Option[Phone],
  address: Option[Address],
  city: Option[City],
  state: Option[State],
  zip: Option[Zip]) {}

object Author {
  implicit val encodeAuthor: Encoder[Author] = (a: Author) => {
    val objects = List(
      Some(ID.toJsonLine(a.id)),
      Some(LastName.toJsonLine(a.lastName)),
      Some(FirstName.toJsonLine(a.firstName)),
      Phone.toJsonLine(a.phone),
      Address.toJsonLine(a.address),
      City.toJsonLine(a.city),
      State.toJsonLine(a.state),
      Zip.toJsonLine(a.zip)
    ).flatten
    Json.obj(objects: _*)
  }

  implicit val decodeAuthor: Decoder[Author] = (c: HCursor) => {
    val id = ID.fromJsonLine(c)
    val lastName = LastName.fromJsonLine(c)
    val firstName = FirstName.fromJsonLine(c)
    val phone = Phone.fromOptionalJsonLine(c)
    val address = Address.fromOptionalJsonLine(c)
    val city = City.fromOptionalJsonLine(c)
    val state = State.fromOptionalJsonLine(c)
    val zip = Zip.fromOptionalJsonLine(c)
    val result: ValidationErrorsOr[Author] =
      (id, lastName, firstName, phone, address, city, state, zip).mapN(Author.apply)
    result match {
      case Invalid(errors) =>
        Left(DecodingFailure(asString(errors), Nil))
      case Valid(author) => Right(author)
    }
  }
}
