package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.fields.{CreateDate, FirstName, ID, LastName, LocationID, UpdateDate, Version}
import org.dka.rdbms.common.model.validation.Validation._

final case class Author(
  override val id: ID,
  override val version: Version,
  lastName: LastName,
  firstName: Option[FirstName],
  locationId: Option[LocationID],
  createDate: CreateDate = CreateDate.now,
  override val lastUpdate: Option[UpdateDate] = None)
  extends Updatable[Author] {

  override def update: Author = this.copy(version = version.next, lastUpdate = UpdateDate.now)

}

object Author {

  implicit val encodeAuthor: Encoder[Author] = (a: Author) => {
    val objects = List(
      Some(ID.toJson(a.id)),
      Some(Version.toJson(a.version)),
      UpdateDate.toJson(a.lastUpdate),
      Some(LastName.toJson(a.lastName)),
      FirstName.toJson(a.firstName),
      LocationID.toJson(a.locationId),
      Some(CreateDate.toJson(a.createDate))
    ).flatten // filter out the None, i.e. only needed lines
    Json.obj(objects: _*)
  }

  implicit val decodeAuthor: Decoder[Author] = (c: HCursor) => {
    val result: ValidationErrorsOr[Author] =
      (
        ID.fromJson(c),
        Version.fromJson(c),
        LastName.fromJson(c),
        FirstName.fromOptionalJson(c),
        LocationID.fromOptionalJson(c),
        CreateDate.fromJson(c),
        UpdateDate.fromOptionalJson(c)
      ).mapN(Author.apply)
    result match {
      case Invalid(errors) =>
        Left(DecodingFailure(errors, Nil))
      case Valid(author) => Right(author)
    }
  }

}
