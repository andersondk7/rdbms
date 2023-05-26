package org.dka.rdbms.common.model.fields

import org.dka.rdbms.common.model.validation.LocalDateTimeValidation

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.temporal.{ChronoUnit, TemporalUnit}

/**
 * createDate requirements:
 *   - must be in format 'YYYY-MM_DD hh:mm:ss.ssssss'
 *   - will only have resolution to milliseconds
 */
final case class CreateDate private (override val value: LocalDateTime) extends Field[LocalDateTime] {

  val asTimestamp: Timestamp = Timestamp.valueOf(value)

}

object CreateDate extends LocalDateTimeValidation[CreateDate] {

  override val fieldName: String = "create_date"

  override def build(tn: LocalDateTime): CreateDate = new CreateDate(tn.truncatedTo(ChronoUnit.MILLIS))

  def build(ts: Timestamp): CreateDate              = new CreateDate(ts.toLocalDateTime)

  def now: CreateDate = CreateDate.build(LocalDateTime.now)

}
