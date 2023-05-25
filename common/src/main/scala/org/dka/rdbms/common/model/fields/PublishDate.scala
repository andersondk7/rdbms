package org.dka.rdbms.common.model.fields

import org.dka.rdbms.common.model.validation.LocalDateValidation

import java.time.LocalDate

/**
 * publishDate requirements:
 *   - can't be empty
 *   - must be in format 'YYYY-MM_DD'
 */
final case class PublishDate private (override val value: LocalDate) extends Field[LocalDate]

object PublishDate extends LocalDateValidation[PublishDate] {
  override val fieldName: String = "publish_date"

  override def build(tn: LocalDate): PublishDate = new PublishDate(tn)
  def fromOpt(o: Option[LocalDate]): Option[PublishDate] = o.map(build)

}
