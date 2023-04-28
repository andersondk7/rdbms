package org.dka.rdbms.common.model

import io.circe._

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

  implicit val decodeID: Decoder[Author] = (c: HCursor) =>
    // todo: this will fail on the first error...
    for {
      id <- ID.fromJsonLine(c)
      lastName <- LastName.fromJsonLine(c)
      firstName <- FirstName.fromJsonLine(c)
      phone <- Phone.fromOptionalJsonLine(c)
      address <- Address.fromOptionalJsonLine(c)
      city <- City.fromOptionalJsonLine(c)
      state <- State.fromOptionalJsonLine(c)
      zip <- Zip.fromOptionalJsonLine(c)
    } yield Author(
      id,
      lastName,
      firstName,
      phone,
      address,
      city,
      state,
      zip
    )
}
