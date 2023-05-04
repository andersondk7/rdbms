package org.dka.rdbms.common.model.components

import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.StringLengthValidation

/**
 * state requirements:
 *   - must be 2 characters
 */

final case class State private (override val value: String) extends Item[String]

object State extends StringLengthValidation[State] {
  val maxLength = 2
  val minLength = 2
  val fieldName: String = "state"

  def build(s: String): State = new State(s)
}
