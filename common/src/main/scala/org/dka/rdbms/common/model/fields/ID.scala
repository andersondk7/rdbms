package org.dka.rdbms.common.model.fields

import org.dka.rdbms.common.model.validation.UUIDValidation

import java.util.UUID

final case class ID private (override val value: UUID) extends Field[UUID]

object ID extends UUIDValidation[ID] {

  override val fieldName: String = "ID"

  override def build(id: UUID): ID =
    new ID(id)

  def build: ID = new ID(UUID.randomUUID())

  def build(uuid: String) = new ID(UUID.fromString(uuid))

}
