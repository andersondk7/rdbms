package org.dka.rdbms.model

import io.circe._

final case class State(value: String) extends StringItem {
  override val fieldName: String = State.fieldName
}

object State {
  val fieldName: String = "state"

  def apply(o: Option[String]): Option[State] = o.map(State(_))

  def toJsonLine(item: State): (String, Json) = (fieldName, Json.fromString(item.value))
  def toJsonLine(item: Option[State]): Option[(String, Json)] = item.map(toJsonLine)

  def fromJsonLine(c: HCursor): Either[DecodingFailure, State] = StringItem.fromJsonLine(c, fieldName)(apply)

  def fromOptionalJsonLine(c: HCursor): Either[DecodingFailure, Option[State]] =
    StringItem.fromOptionalJsonLine(c, fieldName)(apply)
}
