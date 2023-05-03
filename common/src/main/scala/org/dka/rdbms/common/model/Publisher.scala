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

  implicit val decodePublisher: Decoder[Publisher] = (c: HCursor) =>
    (
      ID.fromJsonLine(c),
      CompanyName.fromJsonLine(c),
      Address.fromOptionalJsonLine(c),
      City.fromOptionalJsonLine(c),
      State.fromOptionalJsonLine(c),
      Zip.fromOptionalJsonLine(c)
    ).mapN(Publisher.apply) match {
      case Invalid(errors) => Left(DecodingFailure(errors, Nil))
      case Valid(publisher) => Right(publisher)
    }
}
