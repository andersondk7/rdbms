package org.dka.rdbms.dao

import org.dka.rdbms.model.Author
import slick.dbio.DBIO
import slick.lifted.TableQuery
import slick.model.Table

import scala.concurrent.{ExecutionContext, Future}

/**
 * holds methods to create, update, query, and delete Authors
 *
 * this interface is db agnostic and allows for easy unit testing since an database is not required
 */
trait AuthorDao extends CrudDao[Author, String]{
  val tableQuery = TableQuery[Authors.AuthorTable]

}
