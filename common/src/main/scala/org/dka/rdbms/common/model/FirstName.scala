package org.dka.rdbms.common.model

/**
 * first name requirements:
 *   - can't be empty
 *   - can not be more than 20
 */
final case class FirstName private (override val value: String) extends Item[String]

object FirstName extends StringLengthValidation[FirstName] {
  override val maxLength = 20
  override val minLength = 1
  override val fieldName: String = "firstName"

  override def build(fn: String): FirstName = new FirstName(fn)
}
