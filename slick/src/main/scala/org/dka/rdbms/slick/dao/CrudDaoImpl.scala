package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao._
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

/**
 * Implementation of a CrudDao using an instance of the database
 * @tparam D
 *   domain object stored
 * @tparam I
 *   id type of domain object
 */
trait CrudDaoImpl[D, I] extends CrudDao[D, I] {
  def db: Database
  def singleInsertQuery: D => DBIO[Int]
  def multipleInsertQuery: Seq[D] => DBIO[Option[Int]]
  def getQuery: (I, ExecutionContext) => DBIO[Option[D]]
  def deletedQuery: I => DBIO[Int]

  override def create(item: D)(implicit ec: ExecutionContext): Future[Either[DaoException, D]] =
    db.run(singleInsertQuery(item))
      .map { c: Int =>
        if (c == 1) Right(item)
        else Left(InsertException(s"${item.getClass.getName}: could not insert $item"))
      }

  override def create(items: Seq[D])(implicit ec: ExecutionContext): Future[Either[DaoException, Int]] =
    db.run(multipleInsertQuery(items)).map {
      case Some(count) => Right(count)
      case None =>
        Left(InsertException(s"${items.getClass.getName} could not insert ${items.size}"))
    }

  override def read(id: I)(implicit ec: ExecutionContext): Future[Either[DaoException, Option[D]]] =
    // note this only gets the first, assume that since id is the primary key, there will only be one!
    db.run(getQuery(id, ec)).map(r => Right(r)) // don't know how to capture when it fails...

  override def delete(id: I)(implicit ec: ExecutionContext): Future[Either[DaoException, Option[I]]] =
    db.run(deletedQuery(id)).map {
      case 0 => Right(None)
      case _ =>
        Right(Some(id)) // again assumes that since id is the primary key, there will only be one
    }

}
