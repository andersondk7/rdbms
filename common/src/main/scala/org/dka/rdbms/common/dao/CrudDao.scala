package org.dka.rdbms.common.dao

import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.ID
import scala.concurrent.{ExecutionContext, Future}

/**
 * holds methods to create, update, query, and delete Authors
 *
 * this interface is db agnostic and allows for easy unit testing since an database is not required
 *
 * the implicit ExecutionContext is the context used for '''only for''':
 *   - processing of results into domain objects it is ''' not ''' used for database interaction (calling the driver)
 *
 * @tparam D
 *   domain object stored
 */
trait CrudDao[D] {

  def create(item: D)(implicit ec: ExecutionContext): Future[DaoErrorsOr[D]]

  def create(items: Seq[D])(implicit ec: ExecutionContext): Future[DaoErrorsOr[Int]]

  def read(id: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Option[D]]]

  def delete(id: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Option[ID]]]

  def update(item: D)(implicit ec: ExecutionContext): Future[DaoErrorsOr[D]]

}
