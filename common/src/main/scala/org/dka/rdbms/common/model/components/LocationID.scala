package org.dka.rdbms.common.model.components

import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.UUIDValidation

import java.util.UUID

final case class LocationID private (override val value: UUID) extends Item[UUID]

object LocationID extends UUIDValidation[LocationID] {
  override val fieldName: String = "location_id"

  override def build(id: UUID): LocationID = new LocationID(id)

  def fromOpt(opt: Option[String]): Option[LocationID] = opt.map(s => build(UUID.fromString(s)))
  def build: LocationID = new LocationID(UUID.randomUUID())
}
