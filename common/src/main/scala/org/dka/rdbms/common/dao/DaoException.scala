package org.dka.rdbms.common.dao

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
