package org.dka.rdbms.common.model

import cats.implicits._
import cats.data.Validated._
import io.circe._
import Validation._

final case class Publisher(
  id: ID,
  name: CompanyName,
  address: Option[Address],
  city: Option[City],
  state: Option[State],
  zip: Option[Zip])

object Publisher {

  implicit val encodePublisher: Encoder[Publisher] = (a: Publisher) => {
    val objects = List(
      Some(ID.toJsonLine(a.id)),
      Some(CompanyName.toJsonLine(a.name)),
      Address.toJsonLine(a.address),
      City.toJsonLine(a.city),
      State.toJsonLine(a.state),
      Zip.toJsonLine(a.zip)
    ).flatten
    Json.obj(objects: _*)
  }

  implicit val decodePubisherD: Decoder[Publisher] = (c: HCursor) => {
    val id = ID.fromJsonLine(c)
    val name = CompanyName.fromJsonLine(c)
    val address = Address.fromOptionalJsonLine(c)
    val city = City.fromOptionalJsonLine(c)
    val state = State.fromOptionalJsonLine(c)
    val zip = Zip.fromOptionalJsonLine(c)
    val result = (id, name, address, city, state, zip).mapN(Publisher.apply)
    result match {
      case Invalid(errors) => Left(DecodingFailure(asString(errors), Nil))
      case Valid(publisher) => Right(publisher)
    }
  }
}
