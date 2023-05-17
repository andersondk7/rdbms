package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.fields.{ID, Price, PublishDate, PublisherID, Title, Version}
import org.dka.rdbms.common.model.validation.Validation._
import org.dka.rdbms.common.model.query.BookAuthorSummary

final case class Book(
  override val id: ID,
  override val version: Version,
  title: Title,
  price: Price,
  publisherID: Option[PublisherID],
  publishDate: Option[PublishDate])
  extends Updatable[Book] {
  override def update: Book = this.copy(version = version.next)

}

object Book {
  implicit val encodeTitle: Encoder[Book] = (b: Book) => {
    val objects = List(
      Some(ID.toJson(b.id)),
      Some(Version.toJson(b.version)),
      Some(Title.toJson(b.title)),
      Some(Price.toJson(b.price)),
      PublisherID.toJson(b.publisherID),
      PublishDate.toJson(b.publishDate)
    ).flatten // filter out the None, i.e. only needed lines
    val json = Json.obj(objects: _*)
    json
  }

  implicit val decodeTitle: Decoder[Book] = (c: HCursor) => {
    val result: ValidationErrorsOr[Book] =
      (
        ID.fromJson(c),
        Version.fromJson(c),
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
