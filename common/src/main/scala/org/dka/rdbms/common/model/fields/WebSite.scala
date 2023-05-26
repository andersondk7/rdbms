package org.dka.rdbms.common.model.fields

import org.dka.rdbms.common.model.validation.StringLengthValidation

/**
 * website requirements:
 *   - can not be more than 60
 */
final case class WebSite private (override val value: String) extends Field[String]

object WebSite extends StringLengthValidation[WebSite] {

  override val maxLength = 60

  override val minLength = 1

  override val fieldName: String = "website"

  override def build(c: String): WebSite = new WebSite(c)

  def fromOpt(o: Option[String]): Option[WebSite] = o.map(build)

}
