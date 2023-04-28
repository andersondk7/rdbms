package org.dka.rdbms.common.model

import io.circe._

/**
 * first name requirements:
 *   - can't be empty
 *   - can be optional
 *   - can not be more than 20
 */
final case class FirstName private (value: String)


object FirstName {
  val maxLength = 20
  val minLength = 1

  def build(fn: String): FirstName = new FirstName(fn)
  def apply(name: String): Either[ValidationException, FirstName] =
    StringValidator.lengthValidation(name, fieldName, minLength, maxLength)(build)

  val fieldName: String = "firstName"

  def toJsonLine(item: FirstName): (String, Json) = (fieldName, Json.fromString(item.value))

  def toJsonLine(item: Option[FirstName]): Option[(String, Json)] = item.map(toJsonLine)

  def fromJsonLine(c: HCursor): Either[DecodingFailure, FirstName] = StringItem.fromJsonLineVal(c, fieldName)(apply)

  def fromOptionalJsonLine(c: HCursor): Either[DecodingFailure, Option[FirstName]] =
    StringItem.fromOptionalJsonLineVal(c, fieldName)(apply)
}
