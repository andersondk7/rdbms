package org.dka.rdbms.common.model.components

import cats.data.Validated._
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import io.circe.{DecodingFailure, HCursor}
import org.dka.rdbms.common.model.item.Item
import org.dka.rdbms.common.model.validation.Validation.ValidationErrorsOr
import org.dka.rdbms.common.model.validation.{BigDecimalValidation, JsonParseException, NumberTooSmallException}

/**
 * titleName requirements:
 *   - can't be empty
 *   - can not be more than 30
 */
final case class Price private (override val value: BigDecimal) extends Item[BigDecimal]

object Price extends BigDecimalValidation[Price] {
  override val fieldName: String = "price"

  override def build(amount: BigDecimal): Price = new Price(amount)
  override def validate(string: String): ValidationErrorsOr[Price] = {
    val x: ValidationErrorsOr[Price] = super.validate(string)
    x match {
      case Invalid(ve) => Invalid(ve)
      case Valid(p) =>
        if (p.value <= 0) NumberTooSmallException(fieldName, 0).invalidNec
        else Valid(p)
    }
  }

  def fromJsonLine(
    c: HCursor
  ): ValidationErrorsOr[Price] = {
    val value: Either[DecodingFailure, String] = for {
      string <- c.downField(fieldName).as[String]
    } yield string
    value.fold(
      df => JsonParseException(df).invalidNec,
      input => validate(input) // convert string to Item, converting ValidationException to DecodingFailure
    )
  }
}
