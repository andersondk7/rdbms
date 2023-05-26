package org.dka.rdbms.common.model.fields

import org.dka.rdbms.common.model.validation.StringLengthValidation

final case class Zip private (override val value: String) extends Field[String]

object Zip extends StringLengthValidation[Zip] {

  override val maxLength = 10

  override val minLength = 5

  override val fieldName: String = "zip"

  override def build(zip: String): Zip = new Zip(zip)

}
