package org.dka.rdbms.common.model.validation

import cats.data.Validated._
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import io.circe._
import org.dka.rdbms.common.model.item.Item

import scala.util.{Failure, Success, Try}

trait BigDecimalValidation[T <: Item[BigDecimal]] extends Validation[String, BigDecimal, T] {
  import Validation._

  def validate(string: String): ValidationErrorsOr[T] =
    Try(BigDecimal(string)) match {
      case Failure(t) => InvalidNumberException(fieldName, string, t).invalidNec
      case Success(date) => Valid(build(date))
    }

  def toJsonLine(item: T): (String, Json) = (fieldName, Json.fromString(item.value.toString))

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
