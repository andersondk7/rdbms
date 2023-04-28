package org.dka.rdbms.common.model

import io.circe._

final case class Phone(override val value: String) extends StringItem {
  override val fieldName: String = Phone.fieldName
}

object Phone {
  val fieldName: String = "phone"
  def apply(o: Option[String]): Option[Phone] = o.map(Phone(_))

  def toJsonLine(item: Phone): (String, Json) = (fieldName, Json.fromString(item.value))

  def toJsonLine(item: Option[Phone]): Option[(String, Json)] = item.map(toJsonLine)
  def fromJsonLine(c: HCursor): Either[DecodingFailure, Phone] = StringItem.fromJsonLine(c, fieldName)(apply)
  def fromOptionalJsonLine(c: HCursor): Either[DecodingFailure, Option[Phone]] =
    StringItem.fromOptionalJsonLine(c, fieldName)(apply)
}
