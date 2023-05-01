package org.dka.rdbms.common.model

/**
 * state requirements:
 *   - must be 2 characters
 */

final case class State private (override val value: String) extends Item[String]

object State extends StringValidated[State] {
  val maxLength = 2
  val minLength = 2
  val fieldName: String = "state"

  def build(s: String): State = new State(s)
}
