package org.dka.rdbms.common.model

import io.circe._

trait StringValidated[T <: Item[String]] {
  val maxLength: Int
  val minLength: Int
  val fieldName: String

  def build(c: String): T
  def build(o: Option[String]): Option[T] = o.map(build)

  def apply(s: String): Either[ValidationException, T] = validate(s)

  def apply(o: Option[String]): Either[ValidationException, Option[T]] = validateOption(o)

  def toJsonLine(item: T): (String, Json) = (fieldName, Json.fromString(item.value))
  def toJsonLine(item: Option[T]): Option[(String, Json)] = item.map(toJsonLine)

  protected def validate(string: String): Either[ValidationException, T] =
    string match {
      case _ if string.length < minLength => Left(TooShortException(fieldName, minLength))
      case _ if string.length > maxLength => Left(TooLongException(fieldName, maxLength))
      case s => Right(build(s))
    }

  protected def validateOption(o: Option[String]): Either[ValidationException, Option[T]] = o match {
    case None => Right(None)
    case Some(s) =>
      val validated = validate(s)
      validated.map(Some(_))
  }

  def fromJsonLine(
    c: HCursor
  ): Either[DecodingFailure, T] = {
    val value: Either[DecodingFailure, String] = for {
      string <- c.downField(fieldName).as[String]
    } yield string
    value.fold(
      df => Left(df), // keep DecodingFailure
      string => // convert string to Item, converting ValidationException to DecodingFailure
        validate(string) match {
          case Left(ve) => Left(DecodingFailure(ve.reason, Nil))
          case Right(value) => Right(value)
        }
    )
  }

  def fromOptionalJsonLine(c: HCursor): Either[DecodingFailure, Option[T]] = {
    val result = for {
      value <- c.downField(fieldName).as[Option[String]]
    } yield value
    result.fold(
      df => Left(df),
      {
        case None => Right(None)
        case Some(value) =>
          validate(value) match {
            case Left(ve) => Left(DecodingFailure(ve.reason, Nil))
            case Right(decoded) => Right(Some(decoded))
          }
      }
    )
  }
}
