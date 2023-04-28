package org.dka.rdbms.common.model

import io.circe._

sealed trait Item[T] {
  def value: T
  def fieldName: String
}

trait StringItem extends Item[String]
object StringItem {
  def toJsonLine(item: StringItem): (String, Json) = (item.fieldName, Json.fromString(item.value))
  def toJsonLine(item: Option[StringItem]): Option[(String, Json)] = item.map(toJsonLine)

  def fromJsonLineVal[T](
    c: HCursor,
    fieldName: String
  )(validator: String => Either[ValidationException, T]
  ): Either[DecodingFailure, T] = {
    val value = for {
      string <- c.downField(fieldName).as[String]
    } yield string
    value match {
      case Left(df) => Left(df)
      case Right(string) =>
        validator(string) match {
          case Left(ve) => Left(DecodingFailure(ve.reason, Nil))
          case Right(value) => Right(value)
        }
    }
  }

  def fromOptionalJsonLineVal[T](
    c: HCursor,
    fieldName: String
  )(validator: String => Either[ValidationException, T]
  ): Either[DecodingFailure, Option[T]] = {
    val opt = for {
      value <- c.downField(fieldName).as[Option[String]]
    } yield value
    opt match {
      case Left(df) => Left(df)
      case Right(o) =>
        o match {
          case None => Right(None)
          case Some(value) =>
            validator(value) match {
              case Left(ve) => Left(DecodingFailure(ve.reason, Nil))
              case Right(decoded) => Right(Some(decoded))
            }
        }
    }
  }

  def fromJsonLine[T](c: HCursor, fieldName: String)(builder: String => T): Either[DecodingFailure, T] = for {
    value <- c.downField(fieldName).as[String]
  } yield builder(value)

  def fromOptionalJsonLine[T](c: HCursor, fieldName: String)(builder: String => T): Either[DecodingFailure, Option[T]] =
    for {
      value <- c.downField(fieldName).as[Option[String]]
    } yield value.map(v => builder(v))
}
