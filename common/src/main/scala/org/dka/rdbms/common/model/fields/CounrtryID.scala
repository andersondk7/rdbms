package org.dka.rdbms.common.model.fields

import org.dka.rdbms.common.model.validation.UUIDValidation

import java.util.UUID

final case class CountryID private (override val value: UUID) extends Field[UUID]

object CountryID extends UUIDValidation[CountryID] {
  override val fieldName: String = "country_id"

  override def build(id: UUID): CountryID = new CountryID(id)

  def build: CountryID = new CountryID(UUID.randomUUID())
  def build(uuid: String) = new CountryID(UUID.fromString(uuid))
}
