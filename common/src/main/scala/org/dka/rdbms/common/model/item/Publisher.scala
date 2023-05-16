package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.fields.{ID, LocationID, PublisherName, Version, WebSite}
import org.dka.rdbms.common.model.validation.Validation._

final case class Publisher(
                            override val id: ID,
                            override val version: Version,
                            publisherName: PublisherName,
                            locationId: Option[LocationID],
                            webSite: Option[WebSite]
                          ) extends Updatable[Publisher] {
  override def update: Publisher = this.copy(version = version.next)
}

object Publisher {

  implicit val encodePublisher: Encoder[Publisher] = (p: Publisher) => {
    val objects = List(
      Some(ID.toJson(p.id)),
      Some(Version.toJson(p.version)),
      Some(PublisherName.toJson(p.publisherName)),
      LocationID.toJson(p.locationId),
      WebSite.toJson(p.webSite)
    ).flatten
    Json.obj(objects: _*)
  }

  implicit val decodePublisher: Decoder[Publisher] = (c: HCursor) =>
    (
      ID.fromJson(c),
      Version.fromJson(c),
      PublisherName.fromJson(c),
      LocationID.fromOptionalJson(c),
      WebSite.fromOptionalJson(c)
    ).mapN(Publisher.apply) match {
      case Invalid(errors) => Left(DecodingFailure(errors, Nil))
      case Valid(publisher) => Right(publisher)
    }
}
