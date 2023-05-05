package org.dka.rdbms.common.model.components

import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.StringLengthValidation

/**
 * website requirements:
 *   - can not be more than 60
 */
final case class WebSite private (override val value: String) extends Item[String]

object WebSite extends StringLengthValidation[WebSite] {
  override val maxLength = 60
  override val minLength = 1
  override val fieldName: String = "country_name"

  override def build(c: String): WebSite = new WebSite(c)
}
