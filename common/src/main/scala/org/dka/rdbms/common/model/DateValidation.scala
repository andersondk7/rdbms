package org.dka.rdbms.common.model

import cats.data.Validated._
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import io.circe._
import org.dka.rdbms.common.model.DateValidation.formatter

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Try, Success, Failure}

trait DateValidation[T <: Item[LocalDate]] extends Validation[String, LocalDate, T] {
  import Validation._

  def validate(string: String): ValidationErrorsOr[T] = {
    Try { LocalDate.parse(string) }
    match {
      case Failure(t) => InvalidDateException(fieldName, t).invalidNec
      case Success(date) => Valid(build(date))
    }
  }

  def toJsonLine(item: T): (String, Json) = (fieldName, Json.fromString(formatter.format(item.value)))

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

object DateValidation {
  val formatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE
}