package org.dka.rdbms.common.model.fields

import cats.implicits.catsSyntaxValidatedIdBinCompat0
import io.circe.{DecodingFailure, HCursor}
import org.dka.rdbms.common.model.validation.Validation.ValidationErrorsOr
import org.dka.rdbms.common.model.validation.{JsonParseException, PositiveIntegerValidation}

/**
 * version requirements:
 *   - can't be empty
 *   - must be positive
 */
final case class Version private (override val value: Int) extends Field[Int]

object Version extends PositiveIntegerValidation[Version] {
  override val fieldName: String = "version"
  val defaultVersion: Version = new Version(1)

  override def build(value: Int): Version = new Version(value)

  def fromJson(
    c: HCursor
  ): ValidationErrorsOr[Version] = {
    val value: Either[DecodingFailure, Int] = for {
      i <- c.downField(fieldName).as[Int]
    } yield i
    value.fold(
      df => JsonParseException(df).invalidNec,
      input => validate(input) // convert string to Item, converting ValidationException to DecodingFailure
    )
  }
}
