package org.dka.rdbms.common.model.components

import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.DateValidation

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
