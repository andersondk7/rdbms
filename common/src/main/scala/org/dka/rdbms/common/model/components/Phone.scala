package org.dka.rdbms.common.model.components

import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.StringLengthValidation

/**
 * phone requirements:
 *   - must be 12 characters
 */
final case class Phone private (override val value: String) extends Item[String]

object Phone extends StringLengthValidation[Phone] {
  override val maxLength = 12
  override val minLength = 12
  override val fieldName: String = "phone"

  override def build(s: String): Phone = new Phone(s)
}
