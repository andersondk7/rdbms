package org.dka.rdbms.common.model

import java.time.LocalDate

/**
 * publishDate requirements:
 *   - can't be empty
 *   - must be in format 'YYYY-MM_DD'
 */
final case class PublishDate private (override val value: LocalDate) extends Item[LocalDate]

object PublishDate extends DateValidation[PublishDate] {
  override val fieldName: String = "publishDate"

  override def build(tn: LocalDate): PublishDate = new PublishDate(tn)
}
