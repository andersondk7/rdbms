package org.dka.rdbms.common.dao

import org.dka.rdbms.common.model.components.ID

import scala.concurrent.{ExecutionContext, Future}

/**
 * holds methods to create, update, query, and delete Authors
 *
 * this interface is db agnostic and allows for easy unit testing since an database is not required
 *
 * @tparam D
 *   domain object stored
 */
trait CrudDao[D] {
  def create(item: D)(implicit ec: ExecutionContext): Future[Either[DaoException, D]]
  def create(items: Seq[D])(implicit ec: ExecutionContext): Future[Either[DaoException, Int]]
  def read(id: ID)(implicit ec: ExecutionContext): Future[Either[DaoException, Option[D]]]
  def delete(id: ID)(implicit ec: ExecutionContext): Future[Either[DaoException, Option[ID]]]
}
