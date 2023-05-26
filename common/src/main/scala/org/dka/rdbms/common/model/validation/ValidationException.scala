package org.dka.rdbms.common.model.validation

import cats.data.NonEmptyChain
import io.circe.DecodingFailure

sealed trait ValidationException extends Throwable {

  val reason: String

  override def getMessage: String = reason

}

final case class JsonParseException(explanation: String, details: String) extends ValidationException {

  override val reason = s" could not parse json because: $explanation, details: $details"

}

object JsonParseException {

  def apply(df: DecodingFailure): JsonParseException = JsonParseException(df.message, df.history.mkString(":"))

}

final case class TooShortException(itemName: String, minLength: Int) extends ValidationException {

  override val reason = s"$itemName must be at least $minLength"

}

final case class TooLongException(itemName: String, maxLength: Int) extends ValidationException {

  override val reason = s"$itemName can't be longer than $maxLength"

}

final case class InvalidIDException(itemName: String, input: String, cause: Throwable) extends ValidationException {

  override val reason = s"$itemName was $input, which is not a valid UUID because $cause"

  override def getCause: Throwable = cause

}

final case class InvalidDateException(itemName: String, cause: Throwable) extends ValidationException {

  override val reason = s"$itemName was not in format YYYY-MM-DD"

  override def getCause: Throwable = cause

}

final case class InvalidNumberException(itemName: String, input: String, cause: Throwable) extends ValidationException {

  override val reason = s"$itemName was $input which is not a valid number"

  override def getCause: Throwable = cause

}

final case class NumberTooLargeException(itemName: String, max: BigDecimal) extends ValidationException {

  override val reason = s"$itemName was greater than $max"

}

final case class NumberTooSmallException(itemName: String, min: BigDecimal) extends ValidationException {

  override val reason = s"$itemName was less than $min"

}

object ValidationException {

  def reasons(chain: NonEmptyChain[ValidationException]): Seq[String] =
    chain.foldLeft(Seq(""))((list, ex) => list :+ ex.reason).tail // get rid of leading empty string

}
