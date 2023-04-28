package org.dka.rdbms.common.model

import io.circe._

/**
 * address requirements:
 *   - can't be empty
 *   - can be optional
 *   - can not be more than 40
 */
final case class Address private (value: String)

object Address {
  val maxLength = 40
  val minLength = 1
  val fieldName: String = "address"

  def build(a: String): Address = new Address(a)
  def apply(address: String): Either[ValidationException, Address] =
    StringValidator.lengthValidation(address, fieldName, minLength, maxLength)(build)

  def toJsonLine(item: Address): (String, Json) = (fieldName, Json.fromString(item.value))

  def toJsonLine(item: Option[Address]): Option[(String, Json)] = item.map(toJsonLine)

  def fromJsonLine(c: HCursor): Either[DecodingFailure, Address] = StringItem.fromJsonLineVal(c, fieldName)(apply)

  def fromOptionalJsonLine(c: HCursor): Either[DecodingFailure, Option[Address]] =
    StringItem.fromOptionalJsonLineVal(c, fieldName)(apply)
}
