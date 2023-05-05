package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.components.{ID, Price, PublishDate, PublisherID, TitleName, WebSite}
import org.dka.rdbms.common.model.validation.Validation._

final case class Title(
  id: ID,
  name: TitleName,
  price: Price,
  publisher: Option[PublisherID],
  publishedDate: Option[PublishDate])

object Title {
  implicit val encodeTitle: Encoder[Title] = (t: Title) => {
    println(s"encodeTitle: title: id: ${t.id}")
    println(s"encodeTitle: json.id: ${ID.toJson(t.id)}")
    val objects = List(
      Some(ID.toJson(t.id)),
      Some(TitleName.toJson(t.name)),
      Some(Price.toJson(t.price)),
      PublisherID.toJson(t.publisher),
      PublishDate.toJson(t.publishedDate)
    ).flatten // filter out the None, i.e. only needed lines
    println(s"objects list: $objects")
    val json = Json.obj(objects: _*)
    json
  }

  implicit val decodeTitle: Decoder[Title] = (c: HCursor) => {
    val result: ValidationErrorsOr[Title] =
      (
        ID.fromJson(c),
        TitleName.fromJson(c),
        Price.fromJson(c),
        PublisherID.fromOptionalJson(c),
        PublishDate.fromOptionalJson(c)
      ).mapN(Title.apply)
    result match {
      case Invalid(errors) =>
        Left(DecodingFailure(errors, Nil))
      case Valid(title) => Right(title)
    }
  }
}
