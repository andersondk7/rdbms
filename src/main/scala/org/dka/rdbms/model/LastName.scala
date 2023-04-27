package org.dka.rdbms.model

import io.circe._

final case class LastName(value: String) extends StringItem {
  override val fieldName: String = LastName.fieldName
}

object LastName {
  val fieldName: String = "lastName"

  def apply(o: Option[String]): Option[LastName] = o.map(LastName(_))
  def toJsonLine(item: LastName): (String, Json) = (fieldName, Json.fromString(item.value))

  def toJsonLine(item: Option[LastName]): Option[(String, Json)] = item.map(toJsonLine)

  def fromJsonLine(c: HCursor): Either[DecodingFailure, LastName] = StringItem.fromJsonLine(c, fieldName)(apply)

  def fromOptionalJsonLine(c: HCursor): Either[DecodingFailure, Option[LastName]] =
    StringItem.fromOptionalJsonLine(c, fieldName)(apply)
}
