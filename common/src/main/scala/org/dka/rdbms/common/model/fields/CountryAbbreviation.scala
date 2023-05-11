package org.dka.rdbms.common.model.fields

import org.dka.rdbms.common.model.validation.StringLengthValidation

/**
 * country requirements:
 *   - can not be more than 40
 */
final case class CountryAbbreviation private (override val value: String) extends Field[String]

object CountryAbbreviation extends StringLengthValidation[CountryAbbreviation] {
  override val maxLength = 5
  override val minLength = 1
  override val fieldName: String = "country_abbreviation"

  override def build(a: String): CountryAbbreviation = new CountryAbbreviation(a)
}
