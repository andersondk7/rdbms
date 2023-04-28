package org.dka.rdbms.common.model

import io.circe._

final case class Address(value: String) extends StringItem {
  override val fieldName: String = Address.fieldName
}

object Address {
  val fieldName: String = "address"

  def apply(o: Option[String]): Option[Address] = o.map(Address(_))
  def toJsonLine(item: Address): (String, Json) = (fieldName, Json.fromString(item.value))

  def toJsonLine(item: Option[Address]): Option[(String, Json)] = item.map(toJsonLine)

  def fromJsonLine(c: HCursor): Either[DecodingFailure, Address] = StringItem.fromJsonLine(c, fieldName)(apply)

  def fromOptionalJsonLine(c: HCursor): Either[DecodingFailure, Option[Address]] =
    StringItem.fromOptionalJsonLine(c, fieldName)(apply)
}
