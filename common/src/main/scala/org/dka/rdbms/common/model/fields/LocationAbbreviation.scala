package org.dka.rdbms.common.model.fields

import org.dka.rdbms.common.model.validation.StringLengthValidation

/**
 * country requirements:
 *   - can not be more than 40
 */
final case class LocationAbbreviation private (override val value: String) extends Field[String]

object LocationAbbreviation extends StringLengthValidation[LocationAbbreviation] {
  override val maxLength = 4
  override val minLength = 1
  override val fieldName: String = "location_abbreviation"

  override def build(a: String): LocationAbbreviation = new LocationAbbreviation(a)
}
