package org.dka.rdbms.model

import scala.concurrent.{ExecutionContext, Future}

/**
 * holds methods to create, update, query, and delete Authors
 *
 * this interface is db agnostic and allows for easy unit testing since
 * an database is not required
 */
trait AuthorDao {

  def insertAuthor(author: Author)(implicit ec: ExecutionContext): Future[Either[DaoException, Author]]
  def insertAuthor(author: Seq[Author])(implicit ec: ExecutionContext): Future[Either[DaoException, Int]]
  def getAuthor(id: String)(implicit ec:ExecutionContext): Future[Either[DaoException, Option[Author]]]
  def deleteAuthor(id: String) (implicit ec: ExecutionContext): Future[Either[DaoException, Option[String]]]

}
