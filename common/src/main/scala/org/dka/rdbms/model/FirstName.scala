package org.dka.rdbms.model

import io.circe._

final case class FirstName(value: String) extends StringItem {
  override val fieldName: String = FirstName.fieldName
}

object FirstName {
  val fieldName: String = "firstName"

  def apply(o: Option[String]): Option[FirstName] = o.map(FirstName(_))
  def toJsonLine(item: FirstName): (String, Json) = (fieldName, Json.fromString(item.value))

  def toJsonLine(item: Option[FirstName]): Option[(String, Json)] = item.map(toJsonLine)

  def fromJsonLine(c: HCursor): Either[DecodingFailure, FirstName] = StringItem.fromJsonLine(c, fieldName)(apply)

  def fromOptionalJsonLine(c: HCursor): Either[DecodingFailure, Option[FirstName]] =
    StringItem.fromOptionalJsonLine(c, fieldName)(apply)
}
