package org.dka.rdbms.common.model.validation

import cats.data.Validated._
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import io.circe._
import org.dka.rdbms.common.model.item.Item

import java.util.UUID
import scala.util.{Failure, Success, Try}

trait UUIDValidation[T <: Item[UUID]] extends Validation[String, UUID, T] {
  import Validation._

  def validate(string: String): ValidationErrorsOr[T] =
    Try(UUID.fromString(string)) match {
      case Failure(t) => InvalidIDException(fieldName, string, t).invalidNec
      case Success(uuid) => Valid(build(uuid))
    }

  def toJsonLine(item: T): (String, Json) = (fieldName, Json.fromString(item.value.toString))

  def fromJsonLine(
    c: HCursor
  ): ValidationErrorsOr[T] = {
    val result: Either[DecodingFailure, String] = for {
      value <- c.downField(fieldName).as[String]
    } yield value
    result.fold(
      df => JsonParseException(df).invalidNec,
      input => validate(input) // convert string to Item, converting ValidationException to DecodingFailure
    )
  }

  def fromOptionalJsonLine(c: HCursor): ValidationErrorsOr[Option[T]] = {
    val result = for {
      value <- c.downField(fieldName).as[Option[String]]
    } yield value
    result.fold(
      df => JsonParseException(df).invalidNec,
      {
        case None => Valid(None)
        case Some(value) =>
          validate(value) match {
            case Invalid(ve) => Invalid(ve)
            case Valid(decoded) => Valid(Some(decoded))
          }
      }
    )
  }
}
