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

  def fromJsonLine[T](c: HCursor, fieldName: String)(builder: String => T): Either[DecodingFailure, T] = for {
    value <- c.downField(fieldName).as[String]
  } yield builder(value)

  def fromOptionalJsonLine[T](c: HCursor, fieldName: String)(builder: String => T): Either[DecodingFailure, Option[T]] =
    for {
      value <- c.downField(fieldName).as[Option[String]]
    } yield value.map(v => builder(v))
}
