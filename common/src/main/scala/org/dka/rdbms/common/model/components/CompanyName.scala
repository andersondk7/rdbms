package org.dka.rdbms.common.model.components

import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.StringLengthValidation

/**
 * company name requirements:
 *   - can't be empty
 *   - can not be more than 40
 */

final case class CompanyName private (override val value: String) extends Item[String]

object CompanyName extends StringLengthValidation[CompanyName] {
  override val minLength: Int = 1
  override val maxLength: Int = 40
  override val fieldName: String = "companyName"

  override def build(cn: String): CompanyName = new CompanyName(cn)
}
