package org.dka.rdbms.common.model.components

import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.StringLengthValidation

/**
 * lastName requirements:
 *   - can't be empty
 *   - can not be more than 40
 */
final case class LastName private (override val value: String) extends Item[String]

object LastName extends StringLengthValidation[LastName] {
  override val maxLength = 40
  override val minLength = 1
  override val fieldName: String = "lastName"

  override def build(ln: String): LastName = new LastName(ln)
}
