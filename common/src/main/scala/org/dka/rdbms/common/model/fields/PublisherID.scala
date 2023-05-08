package org.dka.rdbms.common.model.fields

import org.dka.rdbms.common.model.validation.UUIDValidation

import java.util.UUID

final case class PublisherID private (override val value: UUID) extends Field[UUID]

object PublisherID extends UUIDValidation[PublisherID] {
  override val fieldName: String = "publisher_id"

  override def build(id: UUID): PublisherID = new PublisherID(id)

  def build: PublisherID = new PublisherID(UUID.randomUUID())
}
