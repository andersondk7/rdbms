package org.dka.rdbms.common.model.components

import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.UUIDValidation

import java.util.UUID

final case class ID private (override val value: UUID) extends Item[UUID]

object ID extends UUIDValidation[ID] {
  override val fieldName: String = "ID"

  override def build(id: UUID): ID = {
    println(s"building with $id")
    new ID(id)
  }
  def build: ID = new ID(UUID.randomUUID())
}
