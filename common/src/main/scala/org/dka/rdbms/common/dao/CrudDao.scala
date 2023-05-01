package org.dka.rdbms.common.dao

import scala.concurrent.{ExecutionContext, Future}

/**
 * holds methods to create, update, query, and delete Authors
 *
 * this interface is db agnostic and allows for easy unit testing since an database is not required
 */
trait CrudDao[D, I] {
  def create(item: D)(implicit ec: ExecutionContext): Future[Either[DaoException, D]]
  def create(items: Seq[D])(implicit ec: ExecutionContext): Future[Either[DaoException, Int]]
  def read(id: I)(implicit ec: ExecutionContext): Future[Either[DaoException, Option[D]]]
  def delete(id: I)(implicit ec: ExecutionContext): Future[Either[DaoException, Option[I]]]
}
