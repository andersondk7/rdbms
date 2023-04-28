package org.dka.rdbms.common.model

object StringValidator {

  def lengthValidation[T](
    string: String,
    fieldName: String,
    minLength: Int,
    maxLength: Int
  )(builder: String => T
  ): Either[ValidationException, T] =
    string match {
      case _ if string.length < minLength => Left(TooShortException(fieldName, minLength))
      case _ if string.length > maxLength => Left(TooLongException(fieldName, maxLength))
      case s => Right(builder(s))
    }

}
