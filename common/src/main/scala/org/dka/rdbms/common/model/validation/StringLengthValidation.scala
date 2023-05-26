package org.dka.rdbms.common.model.validation

import cats.data.Validated._
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import io.circe._
import Validation.ValidationErrorsOr
import org.dka.rdbms.common.model._
import org.dka.rdbms.common.model.fields.Field

trait StringLengthValidation[T <: Field[String]] extends Validation[String, String, T] {

  val maxLength: Int

  val minLength: Int

  def validate(string: String): ValidationErrorsOr[T] =
    string match {
      case _ if string.length < minLength => TooShortException(fieldName, minLength).invalidNec
      case _ if string.length > maxLength => TooLongException(fieldName, maxLength).invalidNec
      case s                              => Valid(build(s))
    }

  def toJson(item: T): (String, Json) = (fieldName, Json.fromString(item.value))

  def fromJson(
    c: HCursor
  ): ValidationErrorsOr[T] = {
    val value: Either[DecodingFailure, String] = for {
      string <- c.downField(fieldName).as[String]
    } yield string
    value.fold(
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
