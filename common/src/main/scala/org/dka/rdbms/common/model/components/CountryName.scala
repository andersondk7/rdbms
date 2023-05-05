package org.dka.rdbms.common.model.components

import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.StringLengthValidation

/**
 * country requirements:
 *   - can not be more than 40
 */
final case class CountryName private (override val value: String) extends Item[String]

object CountryName extends StringLengthValidation[CountryName] {
  override val maxLength = 40
  override val minLength = 1
  override val fieldName: String = "country_name"

  override def build(c: String): CountryName = new CountryName(c)
}
