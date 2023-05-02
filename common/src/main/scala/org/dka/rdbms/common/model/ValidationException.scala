package org.dka.rdbms.common.model

import cats.data.{NonEmptyChain, Validated, ValidatedNec}
import cats.data.Validated._
import io.circe.DecodingFailure

import scala.language.implicitConversions

object Validation {
  type ValidationErrorsOr[T] = ValidatedNec[ValidationException, T]
  type DecodeErrorsOr[T] = Either[DecodingFailure, T]
  def asList(errors: NonEmptyChain[ValidationException]): List[String] =
    errors.tail.foldLeft(List(errors.head.reason))((acc, ve) => ve.reason :: acc)
  def asString(errors: NonEmptyChain[ValidationException]): String = asList(errors).mkString(" : ")
}
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
