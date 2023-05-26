package org.dka.rdbms.common.model.fields

import org.dka.rdbms.common.model.validation.StringLengthValidation

/**
 * country requirements:
 *   - can not be more than 40
 */
final case class LocationName private (override val value: String) extends Field[String]

object LocationName extends StringLengthValidation[LocationName] {

  override val maxLength = 40

  override val minLength = 1

  override val fieldName: String = "location_name"

  override def build(l: String): LocationName = new LocationName(l)

}
