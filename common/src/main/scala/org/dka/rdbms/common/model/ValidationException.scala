package org.dka.rdbms.common.model

import cats.data.{NonEmptyChain, Validated, ValidatedNec}
import cats.data.Validated._
import io.circe.DecodingFailure

import scala.language.implicitConversions

sealed trait ValidationException extends Throwable {
  val reason: String
  override def getMessage: String = reason
}

case class JsonParseException(explaination: String, details: String) extends ValidationException {
  override val reason = s" could not parse json because: $explaination, details: $details"
}

object JsonParseException {
  def apply(df: DecodingFailure): JsonParseException = JsonParseException(df.message, df.history.mkString(":"))
}
case class TooShortException(itemName: String, minLength: Int) extends ValidationException {
  override val reason = s"$itemName must be at least $minLength"
}

case class TooLongException(itemName: String, maxLength: Int) extends ValidationException {
  override val reason = s"$itemName can't be longer than $maxLength"
}

case class InvalidIDException(itemName: String, input: String, cause: Throwable) extends ValidationException {
  override val reason = s"$itemName was $input, which is not a valid UUID because $cause"
  override def getCause: Throwable = cause
}

case class InvalidDateException(itemName: String, cause: Throwable) extends ValidationException {
  override val reason = s"$itemName was not in format YYYY-MM-DD"
  override def getCause: Throwable = cause
}

case class InvalidNumberException(itemName: String, input: String, cause: Throwable) extends ValidationException {
  override val reason = s"$itemName was $input which is not a valid number"
  override def getCause: Throwable = cause
}

case class NumberTooLargeException(itemName: String, max: BigDecimal) extends ValidationException {
  override val reason = s"$itemName was greater than $max"
}

case class NumberTooSmallException(itemName: String, min: BigDecimal) extends ValidationException {
  override val reason = s"$itemName was less than $min"
}
