package org.dka.rdbms.common.model.fields

import org.dka.rdbms.common.model.validation.StringLengthValidation

/**
 * first name requirements:
 *   - can't be empty
 *   - can not be more than 20
 */
final case class FirstName private (override val value: String) extends Field[String]

object FirstName extends StringLengthValidation[FirstName] {

  override val maxLength = 20

  override val minLength = 1

  override val fieldName: String = "first_name"

  override def build(fn: String): FirstName = new FirstName(fn)

  def fromOpt(o: Option[String]): Option[FirstName] = o.map(build)

}
