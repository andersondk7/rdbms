package org.dka.rdbms.common.model

/**
 * address requirements:
 *   - can't be empty
 *   - can not be more than 40
 */
final case class Address private (override val value: String) extends Item[String]

object Address extends StringLengthValidation[Address] {
  override val maxLength = 40
  override val minLength = 1
  override val fieldName: String = "address"

  override def build(a: String): Address = new Address(a)

}
