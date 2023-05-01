package org.dka.rdbms.common.model

/**
 * city requirements:
 *   - can't be empty
 *   - can not be more than 40
 */
final case class City private (override val value: String) extends Item[String]

object City extends StringValidated[City] {
  override val maxLength = 40
  override val minLength = 1
  override val fieldName: String = "city"

  override def build(c: String): City = new City(c)
}
