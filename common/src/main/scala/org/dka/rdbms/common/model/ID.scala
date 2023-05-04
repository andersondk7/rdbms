package org.dka.rdbms.common.model

import java.util.UUID

final case class ID private (override val value: UUID) extends Item[UUID]

object ID extends UUIDValidation[ID] {
  override val fieldName: String = "ID"

  override def build(id: UUID): ID = new ID(id)
  def build: ID = new ID(UUID.randomUUID())
}
