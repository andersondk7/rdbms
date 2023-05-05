package org.dka.rdbms.common.model.components

import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.UUIDValidation

import java.util.UUID

final case class PublisherID private (override val value: UUID) extends Item[UUID]

object PublisherID extends UUIDValidation[PublisherID] {
  override val fieldName: String = "publisher_id"

  override def build(id: UUID): PublisherID = new PublisherID(id)

  def build: PublisherID = new PublisherID(UUID.randomUUID())
}
