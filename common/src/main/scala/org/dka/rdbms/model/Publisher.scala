package org.dka.rdbms.model

import io.circe._

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

  implicit val decodeID: Decoder[Publisher] = (c: HCursor) =>
    for {
      id <- ID.fromJsonLine(c)
      name <- CompanyName.fromJsonLine(c)
      address <- Address.fromOptionalJsonLine(c)
      city <- City.fromOptionalJsonLine(c)
      state <- State.fromOptionalJsonLine(c)
      zip <- Zip.fromOptionalJsonLine(c)
    } yield Publisher(
      id,
      name,
      address,
      city,
      state,
      zip
    )
}
