package org.dka.rdbms.common.model

sealed trait ValidationException extends Throwable {
  val reason: String
  override def getMessage: String = reason
}

case class TooShortException(itemName: String, minLength: Int) extends ValidationException {
  override val reason = s"$itemName must be at least $minLength"
}

case class TooLongException(itemName: String, maxLength: Int) extends ValidationException {
  override val reason = s"$itemName can't be longer than $maxLength"
}
