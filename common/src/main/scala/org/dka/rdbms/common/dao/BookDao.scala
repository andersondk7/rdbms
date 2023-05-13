package org.dka.rdbms.common.dao

import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.ID
import org.dka.rdbms.common.model.item.Book
import org.dka.rdbms.common.model.query.BookAuthorSummary

import scala.concurrent.{ExecutionContext, Future}

/**
 * adds methods beyond simple crud stuff anticipated to be mostly specific queries
 *
 * this interface is db agnostic and allows for easy unit testing since an database is not required
 */
trait BookDao extends CrudDao[Book] {
  /**
   * get the ids of all books
   * // should be a stream!!!
   */
  def getAllIds(implicit ec: ExecutionContext): Future[DaoErrorsOr[Seq[ID]]]

  /**
   * for a given book, return all the authors, and order
   */
  def getAuthorsForBook(bookId: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Seq[BookAuthorSummary]]]
//  def getAuthorsForBookSql(bookId: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Seq[BookAuthorSummary]]]
}
