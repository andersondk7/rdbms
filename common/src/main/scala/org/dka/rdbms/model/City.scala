package org.dka.rdbms.model

import io.circe._

final case class City(value: String) extends StringItem {
  override val fieldName: String = City.fieldName
}

object City {
  val fieldName: String = "city"
  def apply(o: Option[String]): Option[City] = o.map(City(_))
  def toJsonLine(item: City): (String, Json) = (fieldName, Json.fromString(item.value))
  def toJsonLine(item: Option[City]): Option[(String, Json)] = item.map(toJsonLine)

  def fromJsonLine(c: HCursor): Either[DecodingFailure, City] = StringItem.fromJsonLine(c, fieldName)(City.apply)
  def fromOptionalJsonLine(c: HCursor): Either[DecodingFailure, Option[City]] =
    StringItem.fromOptionalJsonLine(c, fieldName)(apply)
}
