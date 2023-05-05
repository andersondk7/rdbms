package org.dka.rdbms.common.model.components

import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.StringLengthValidation

/**
 * titleName requirements:
 *   - can't be empty
 *   - can not be more than 30
 */
final case class TitleName private (override val value: String) extends Item[String]

object TitleName extends StringLengthValidation[TitleName] {
  override val maxLength = 200
  override val minLength = 1
  override val fieldName: String = "titleName"

  override def build(tn: String): TitleName = new TitleName(tn)
}
