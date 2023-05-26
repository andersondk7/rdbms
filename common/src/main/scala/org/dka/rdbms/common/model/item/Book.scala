package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.fields.{CreateDate, ID, Price, PublishDate, PublisherID, Title, UpdateDate, Version}
import org.dka.rdbms.common.model.validation.Validation._

final case class Book(
  override val id: ID,
  override val version: Version,
  title: Title,
  price: Price,
  publisherID: Option[PublisherID],
  publishDate: Option[PublishDate],
  createDate: CreateDate = CreateDate.now,
  override val lastUpdate: Option[UpdateDate] = None)
  extends Updatable[Book] {

  override def update: Book = this.copy(version = version.next, lastUpdate = UpdateDate.now)

}

object Book {

  implicit val encodeTitle: Encoder[Book] = (b: Book) => {
    val objects = List(
      Some(ID.toJson(b.id)),
      Some(Version.toJson(b.version)),
      UpdateDate.toJson(b.lastUpdate),
      Some(Title.toJson(b.title)),
      Some(Price.toJson(b.price)),
      PublisherID.toJson(b.publisherID),
      PublishDate.toJson(b.publishDate),
      Some(CreateDate.toJson(b.createDate))
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
        PublishDate.fromOptionalJson(c),
        CreateDate.fromJson(c),
        UpdateDate.fromOptionalJson(c)
      ).mapN(Book.apply)
    result match {
      case Invalid(errors) =>
        Left(DecodingFailure(errors, Nil))
      case Valid(title) => Right(title)
    }
  }

}
