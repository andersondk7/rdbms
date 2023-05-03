package org.dka.rdbms.common.model

import cats.data.Validated._
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import io.circe._

trait StringValidated[T <: Item[String]] {
  import Validation._
  val maxLength: Int
  val minLength: Int
  val fieldName: String

  def build(c: String): T
  def build(o: Option[String]): Option[T] = o.map(build)

  def apply(s: String): ValidationErrorsOr[T] = validate(s)

  def apply(o: Option[String]): ValidationErrorsOr[Option[T]] = validateOption(o)

  def toJsonLine(item: T): (String, Json) = (fieldName, Json.fromString(item.value))
  protected def validate(string: String): ValidationErrorsOr[T] =
    string match {
      case _ if string.length < minLength => TooShortException(fieldName, minLength).invalidNec
      case _ if string.length > maxLength => TooLongException(fieldName, maxLength).invalidNec
      case s => Valid(build(s))
    }

  def toJsonLine(item: Option[T]): Option[(String, Json)] = item.map(toJsonLine)

  protected def validateOption(o: Option[String]): ValidationErrorsOr[Option[T]] = o match {
    case None => Valid(None)
    case Some(s) =>
      val validated = validate(s)
      validated.map(Some(_))
  }

  def fromJsonLine(
    c: HCursor
  ): ValidationErrorsOr[T] = {
    val value: Either[DecodingFailure, String] = for {
      string <- c.downField(fieldName).as[String]
    } yield string
    value.fold(
      df => JsonParseException(df).invalidNec,
      string => // convert string to Item, converting ValidationException to DecodingFailure
        validate(string) match {
          case Invalid(ve) => Invalid(ve)
          case Valid(value) => Valid(value)
        }
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
