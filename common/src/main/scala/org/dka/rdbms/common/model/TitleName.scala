package org.dka.rdbms.common.model

/**
 * titleName requirements:
 *   - can't be empty
 *   - can not be more than 30
 */
final case class TitleName private (override val value: String) extends Item[String]

object TitleName extends StringLengthValidation[TitleName] {
  override val maxLength = 30
  override val minLength = 1
  override val fieldName: String = "titleName"

  override def build(tn: String): TitleName = new TitleName(tn)
}
