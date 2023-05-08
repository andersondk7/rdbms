package org.dka.rdbms.common.dao

import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.ID
import org.dka.rdbms.common.model.item.Author
import org.dka.rdbms.common.model.query.BookAuthorSummary

import scala.concurrent.{ExecutionContext, Future}

/**
 * adds methods beyond simple crud stuff anticipated to be mostly specific queries
 *
 * this interface is db agnostic and allows for easy unit testing since an database is not required
 */
trait AuthorDao extends CrudDao[Author] {
  def getAuthorsForBook(bookId: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Seq[BookAuthorSummary]]]
}
