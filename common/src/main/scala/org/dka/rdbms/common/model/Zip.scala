package org.dka.rdbms.common.model

import io.circe._

final case class Zip(value: String) extends StringItem {
  override val fieldName: String = Zip.fieldName
}

object Zip {
  val fieldName: String = "zip"

  def apply(o: Option[String]): Option[Zip] = o.map(Zip(_))
  def toJsonLine(item: Zip): (String, Json) = (fieldName, Json.fromString(item.value))

  def toJsonLine(item: Option[Zip]): Option[(String, Json)] = item.map(toJsonLine)

  def fromJsonLine(c: HCursor): Either[DecodingFailure, Zip] = StringItem.fromJsonLine(c, fieldName)(apply)

  def fromOptionalJsonLine(c: HCursor): Either[DecodingFailure, Option[Zip]] =
    StringItem.fromOptionalJsonLine(c, fieldName)(apply)
}
