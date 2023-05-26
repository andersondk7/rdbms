package org.dka.rdbms.common.model.validation

import cats.data.Validated._
import cats.data.{NonEmptyChain, ValidatedNec}
import io.circe._
import org.dka.rdbms.common.model.fields.Field
import org.dka.rdbms.common.model.validation.Validation._

import scala.language.implicitConversions

/**
 * @tparam I
 *   source type of data
 * @tparam S
 *   type of data held in the item
 * @tparam T
 *   item from source type
 */
trait Validation[I, S, T <: Field[S]] {

  val fieldName: String

  /**
   * @param data
   *   data held in the item
   * @return
   *   Item holding the data
   */
  def build(data: S): T

  def build(o: Option[S]): Option[T] = o.map(build)

  def apply(s: I): ValidationErrorsOr[T] = validate(s)

  def apply(o: Option[I]): ValidationErrorsOr[Option[T]] = validateOption(o)

  /**
   * validate the input intended to be overridden to add specific validations
   */
  def validate(input: I): ValidationErrorsOr[T]

  private def validateOption(o: Option[I]): ValidationErrorsOr[Option[T]] = o match {
    case None => Valid(None)
    case Some(s) =>
      val validated = validate(s)
      validated.map(Some(_))
  }

  /**
   * write the Item as json
   */
  def toJson(item: T): (String, Json)

  def toJson(item: Option[T]): Option[(String, Json)] = item.map(toJson)

  /**
   * read the item from json
   */
  def fromJson(c: HCursor): ValidationErrorsOr[T]

  def fromOptionalJson(c: HCursor): ValidationErrorsOr[Option[T]]

}

object Validation {

  type ValidationErrorsOr[T] = ValidatedNec[ValidationException, T]

  private def asList(errors: NonEmptyChain[ValidationException]): List[String] =
    errors.tail.foldLeft(List(errors.head.reason))((acc, ve) => ve.reason :: acc)

  implicit def asString(errors: NonEmptyChain[ValidationException]): String = asList(errors).mkString(" : ")

}
