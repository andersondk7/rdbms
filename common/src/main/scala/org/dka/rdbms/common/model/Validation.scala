package org.dka.rdbms.common.model

import cats.data.{NonEmptyChain, ValidatedNec}
import cats.data.Validated._
import io.circe._

import Validation._
import scala.language.implicitConversions

/**
 *
 * @tparam I source type of data
 * @tparam S type of data held in the item
 * @tparam T  item from source type
 */
trait Validation[I, S, T <: Item[S] ]{
  val fieldName: String

  def build(c: S): T
  def build(o: Option[S]): Option[T] = o.map(build)

  def apply(s: I): ValidationErrorsOr[T] = validate(s)
  def apply(o: Option[I]): ValidationErrorsOr[Option[T]] = validateOption(o)

  def validate(input: I): ValidationErrorsOr[T]

  private def validateOption(o: Option[I]): ValidationErrorsOr[Option[T]] = o match {
    case None => Valid(None)
    case Some(s) =>
      val validated = validate(s)
      validated.map(Some(_))
  }

  def toJsonLine(item: T): (String, Json)
  def toJsonLine(item: Option[T]): Option[(String, Json)] = item.map(toJsonLine)

  def fromJsonLine( c: HCursor ): ValidationErrorsOr[T]

  def fromOptionalJsonLine(c: HCursor): ValidationErrorsOr[Option[T]]
}

object Validation {
  type ValidationErrorsOr[T] = ValidatedNec[ValidationException, T]

  private def asList(errors: NonEmptyChain[ValidationException]): List[String] =
    errors.tail.foldLeft(List(errors.head.reason))((acc, ve) => ve.reason :: acc)
  implicit def asString(errors: NonEmptyChain[ValidationException]): String = asList(errors).mkString(" : ")
}