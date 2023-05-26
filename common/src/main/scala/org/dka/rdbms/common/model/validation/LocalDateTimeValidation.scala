package org.dka.rdbms.common.model.validation

import cats.data.Validated._
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import io.circe._
import org.dka.rdbms.common.model.fields.Field

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}

trait LocalDateTimeValidation[T <: Field[LocalDateTime]] extends Validation[String, LocalDateTime, T] {

  import Validation._
  import LocalDateTimeValidation._

  def validate(string: String): ValidationErrorsOr[T] =
    Try(LocalDateTime.parse(string)) match {
      case Failure(t)    => InvalidDateException(fieldName, t).invalidNec
      case Success(date) => Valid(build(date))
    }

  def toJson(item: T): (String, Json) =
    (fieldName, Json.fromString(formatter.format(item.value)))

  def fromJson(
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

  def fromOptionalJson(c: HCursor): ValidationErrorsOr[Option[T]] = {
    val result = for {
      value <- c.downField(fieldName).as[Option[String]]
    } yield value
    result.fold(
      df => JsonParseException(df).invalidNec,
      {
        case None => Valid(None)
        case Some(value) =>
          validate(value) match {
            case Invalid(ve)    => Invalid(ve)
            case Valid(decoded) => Valid(Some(decoded))
          }
      }
    )
  }

}

object LocalDateTimeValidation {

  val formatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

}
