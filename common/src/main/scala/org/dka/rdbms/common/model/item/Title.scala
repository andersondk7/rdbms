package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.components.{ID, Price, PublishDate, TitleName}
import org.dka.rdbms.common.model.validation.Validation._

final case class Title(
  id: ID,
  name: TitleName,
  price: Price,
  publisher: Option[ID],
  publishedDate: Option[PublishDate])

object Title {
  implicit val encodeTitle: Encoder[Title] = (t: Title) => {
    val objects = List(
      Some(ID.toJsonLine(t.id)),
      Some(TitleName.toJsonLine(t.name)),
      Some(Price.toJsonLine(t.price)),
      ID.toJsonLine(t.publisher),
      PublishDate.toJsonLine(t.publishedDate)
    ).flatten // filter out the None, i.e. only needed lines
    Json.obj(objects: _*)
  }

  implicit val decodeAuthor: Decoder[Title] = (c: HCursor) => {
    val result: ValidationErrorsOr[Title] =
      (
        ID.fromJsonLine(c),
        TitleName.fromJsonLine(c),
        Price.fromJsonLine(c),
        ID.fromOptionalJsonLine(c),
        PublishDate.fromOptionalJsonLine(c)
      ).mapN(Title.apply)
    result match {
      case Invalid(errors) =>
        Left(DecodingFailure(errors, Nil))
      case Valid(title) => Right(title)
    }
  }
}
