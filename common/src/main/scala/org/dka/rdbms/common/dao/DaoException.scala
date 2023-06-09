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

case class ItemNotFoundException(id: ID, override val underlyingCause: Option[Throwable] = None) extends DaoException {

  override val reason: String = s"could not find $id"

}

case class InvalidVersionException(oldVersion: Version, newVersion: Option[Version] = None) extends DaoException {

  override val underlyingCause: Option[Throwable] = None

  override val reason: String = newVersion.fold(s"attempt to update old version $oldVersion")(nv =>
    s"attempt to update old version $oldVersion with $nv")

}

case class UpdateException(id: ID, override val underlyingCause: Option[Throwable] = None) extends DaoException {

  override val reason: String = s"could not update id: $ID"

}

object Validation {

  // only expect one DaoException, so don't make this a ValidatedNecDaoException, T]
  type DaoErrorsOr[T] = Either[DaoException, T]

}
