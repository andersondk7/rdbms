package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.fields.{ID, Price, PublishDate, PublisherID, Title}
import org.dka.rdbms.common.model.validation.Validation._

final case class Book(
  id: ID,
  title: Title,
  price: Price,
  publisherID: Option[PublisherID],
  publishDate: Option[PublishDate])
//  publishedDate: Option[PublishDate])

object Book {
  implicit val encodeTitle: Encoder[Book] = (t: Book) => {
    println(s"encodeTitle: title: id: ${t.id}")
    println(s"encodeTitle: json.id: ${ID.toJson(t.id)}")
    val objects = List(
      Some(ID.toJson(t.id)),
      Some(Title.toJson(t.title)),
      Some(Price.toJson(t.price)),
      PublisherID.toJson(t.publisherID),
      PublishDate.toJson(t.publishDate)
//      PublishDate.toJson(t.publishedDate)
    ).flatten // filter out the None, i.e. only needed lines
    println(s"objects list: $objects")
    val json = Json.obj(objects: _*)
    json
  }

  implicit val decodeTitle: Decoder[Book] = (c: HCursor) => {
    val result: ValidationErrorsOr[Book] =
      (
        ID.fromJson(c),
        Title.fromJson(c),
        Price.fromJson(c),
        PublisherID.fromOptionalJson(c),
        PublishDate.fromOptionalJson(c)
//        PublishDate.fromOptionalJson(c)
      ).mapN(Book.apply)
    result match {
      case Invalid(errors) =>
        Left(DecodingFailure(errors, Nil))
      case Valid(title) => Right(title)
    }
  }
}
