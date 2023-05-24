package org.dka.rdbms.common.model.fields

import org.dka.rdbms.common.model.validation.LocalDateTimeValidation

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * updateDate requirements:
 *   - must be in format 'YYYY-MM_DD hh:mm:ss.ssssss'
 *   - will only have resolution to milliseconds
 */
final case class UpdateDate private (override val value: LocalDateTime) extends Field[LocalDateTime] {
  val asTimeStamp: Timestamp = Timestamp.valueOf(value)
}

object UpdateDate extends LocalDateTimeValidation[UpdateDate] {
  override val fieldName: String = "update_date"

  override def build(tn: LocalDateTime): UpdateDate = new UpdateDate(tn.truncatedTo(ChronoUnit.MILLIS))
  def build(ts: Timestamp): UpdateDate = new UpdateDate(ts.toLocalDateTime)
  def fromOption(o: Option[LocalDateTime]): Option[UpdateDate] = o.map(ld => new UpdateDate(ld))

  def now: Option[UpdateDate] = Some(UpdateDate.build(LocalDateTime.now))
}
