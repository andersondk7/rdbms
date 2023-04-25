package org.dka.rdbms.dao

import org.dka.rdbms.model.CrudDao
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

/**
 * Implementation of a CrudDao using an instance of the database
 * @param db
 *   database on which actions are run
 * @tparam D
 *   domain object stored
 * @tparam I
 *   id field of domain object
 */
abstract class CrudDaoImpl[D, I](val db: Database) extends CrudDao[D, I] {
  def singleInsertQuery: D => DBIO[Int]
  def multipleInsertQuery: Seq[D] => DBIO[Option[Int]]
  def getQuery: (I, ExecutionContext) => DBIO[Option[D]]
  def deletedQuery: I => DBIO[Int]

  override def insert(item: D)(implicit ec: ExecutionContext): Future[Either[DaoException, D]] =
    db.run(singleInsertQuery(item))
      .map { c: Int =>
        if (c == 1) Right(item)
        else Left(InsertException(s"could not insert $item"))
      }

  override def insert(items: Seq[D])(implicit ec: ExecutionContext): Future[Either[DaoException, Int]] =
    db.run(multipleInsertQuery(items)).map {
      case Some(count) => Right(count)
      case None =>
        Left(InsertException(s"could not insert ${items.size}"))
    }

  override def get(id: I)(implicit ec: ExecutionContext): Future[Either[DaoException, Option[D]]] =
    // note this only gets the first, assume that since id is the primary key, there will only be one!
    db.run(getQuery(id, ec)).map(r => Right(r)) // don't know how to capture when it fails...

  override def delete(id: I)(implicit ec: ExecutionContext): Future[Either[DaoException, Option[I]]] =
    db.run(deletedQuery(id)).map {
      case 0 => Right(None)
      case _ =>
        Right(Some(id)) // again assumes that since id is the primary key, there will only be one
    }

}
