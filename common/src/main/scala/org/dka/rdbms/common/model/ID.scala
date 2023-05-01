package org.dka.rdbms.common.model

final case class ID private (override val value: String) extends Item[String]

object ID extends StringValidated[ID] {
  override val fieldName: String = "ID"
  override val minLength: Int = 1
  override val maxLength: Int = 11

  override def build(id: String): ID = new ID(id)
}
