package org.dka.rdbms.common.config

sealed trait ConfigException extends Throwable {
  val reason: String
  val underlyingCause: Option[Throwable]

  override def getMessage: String = reason

  override def getCause: Throwable = underlyingCause.orNull
}

case class MissingFieldException(fieldName: String, override val underlyingCause: Option[Throwable] = None)
  extends ConfigException {
  override val reason: String = s"missing field: $fieldName"
}

case class InvalidFieldException(
  fieldName: String,
  override val underlyingCause: Option[Throwable] = None)
  extends ConfigException {
  override val reason: String = s"invalid field: $fieldName"
}
