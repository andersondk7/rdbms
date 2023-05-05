package org.dka.rdbms.common.model.components

import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.UUIDValidation

import java.util.UUID

final case class CountryID private (override val value: UUID) extends Item[UUID]

object CountryID extends UUIDValidation[CountryID] {
  override val fieldName: String = "country_id"

  override def build(id: UUID): CountryID = new CountryID(id)

  def build: CountryID = new CountryID(UUID.randomUUID())
}
