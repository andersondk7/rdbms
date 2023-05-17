package org.dka.rdbms.common.dao

import org.dka.rdbms.common.model.fields.{ID, Version}

sealed trait DaoException extends Throwable {
  val reason: String
  val underlyingCause: Option[Throwable]

  override def getMessage: String = reason

  override def getCause: Throwable = underlyingCause.orNull
}

case class DeleteException(override val reason: String, override val underlyingCause: Option[Throwable] = None)
  extends DaoException

case class InsertException(override val reason: String, override val underlyingCause: Option[Throwable] = None)
  extends DaoException

case class QueryException(override val reason: String, override val underlyingCause: Option[Throwable] = None)
  extends DaoException

case class ConfigurationException(reasons: List[String], override val underlyingCause: Option[Throwable] = None)
  extends DaoException {
  override val reason: String = reasons.mkString("\t")
}

case class ItemNotFoundException(val id: ID) extends DaoException {
  override val reason: String = s"could not find $id"
  override val underlyingCause: Option[Throwable] = None
}

case class InvalidVersionException(version: Version) extends DaoException {
  override val underlyingCause: Option[Throwable] = None
  override val reason: String = s"attempt to update old version $version"
}

object Validation {
  type DaoErrorsOr[T] = Either[DaoException, T]
}
