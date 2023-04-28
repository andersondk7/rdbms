package org.dka.rdbms.common.model

import io.circe._

final case class CompanyName(value: String) extends StringItem {
  override val fieldName: String = CompanyName.fieldName
}
object CompanyName {
  val fieldName: String = "companyName"

  def apply(o: Option[String]): Option[CompanyName] = o.map(CompanyName(_))
  def toJsonLine(item: CompanyName): (String, Json) = (fieldName, Json.fromString(item.value))
  def toJsonLine(item: Option[CompanyName]): Option[(String, Json)] = item.map(toJsonLine)
  def fromJsonLine(c: HCursor): Either[DecodingFailure, CompanyName] = StringItem.fromJsonLine(c, fieldName)(apply)
  def fromOptionalJsonLine(c: HCursor): Either[DecodingFailure, Option[CompanyName]] =
    StringItem.fromOptionalJsonLine(c, fieldName)(apply)
}
