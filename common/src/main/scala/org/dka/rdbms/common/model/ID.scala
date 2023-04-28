package org.dka.rdbms.common.model

import io.circe._

final case class ID(value: String) extends StringItem {
  override val fieldName: String = ID.fieldName
}

object ID {
  val fieldName: String = "ID"

  def toJsonLine(item: ID): (String, Json) = (fieldName, Json.fromString(item.value))
  def fromJsonLine(c: HCursor): Either[DecodingFailure, ID] = StringItem.fromJsonLine(c, fieldName)(apply)
  // no implementation for from OptionJsonLine since ID is ** always required **
}
