package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.fields.{ID, LocationID, PublisherName, WebSite}
import org.dka.rdbms.common.model.validation.Validation._

final case class Publisher(
  id: ID,
  name: PublisherName,
  locationId: Option[LocationID],
  webSite: Option[WebSite])

object Publisher {

  implicit val encodePublisher: Encoder[Publisher] = (p: Publisher) => {
    val objects = List(
      Some(ID.toJson(p.id)),
      Some(PublisherName.toJson(p.name)),
      LocationID.toJson(p.locationId),
      WebSite.toJson(p.webSite)
    ).flatten
    Json.obj(objects: _*)
  }

  implicit val decodePublisher: Decoder[Publisher] = (c: HCursor) =>
    (
      ID.fromJson(c),
      PublisherName.fromJson(c),
      LocationID.fromOptionalJson(c),
      WebSite.fromOptionalJson(c)
    ).mapN(Publisher.apply) match {
      case Invalid(errors) => Left(DecodingFailure(errors, Nil))
      case Valid(publisher) => Right(publisher)
    }
}
