package org.dka.rdbms.slick.dao

import org.dka.rdbms.common.dao._
import org.dka.rdbms.common.model.components.ID
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

/**
 * Implementation of a CrudDao using an instance of the database
 * @tparam D
 *   domain object stored
 */
trait CrudDaoImpl[D] extends CrudDao[D] {
  def db: Database

  //
  // crud IO operations
  //
  def singleInsertIO: D => DBIO[Int]
  def multipleInsertIO: Seq[D] => DBIO[Option[Int]]
  def getIO: (ID, ExecutionContext) => DBIO[Option[D]]
  def deletedIO: ID => DBIO[Int]

  override def create(item: D)(implicit ec: ExecutionContext): Future[Either[DaoException, D]] =
    db.run(singleInsertIO(item))
      .map { c: Int =>
        if (c == 1) Right(item)
        else Left(InsertException(s"${item.getClass.getName}: could not insert $item"))
      }

  override def create(items: Seq[D])(implicit ec: ExecutionContext): Future[Either[DaoException, Int]] =
    db.run(multipleInsertIO(items)).map {
      case Some(count) => Right(count)
      case None =>
        Left(InsertException(s"${items.getClass.getName} could not insert ${items.size}"))
    }

  override def read(id: ID)(implicit ec: ExecutionContext): Future[Either[DaoException, Option[D]]] =
    // note this only gets the first, assume that since id is the primary key, there will only be one!
    db.run(getIO(id, ec)).map(r => Right(r)) // don't know how to capture when it fails...

  override def delete(id: ID)(implicit ec: ExecutionContext): Future[Either[DaoException, Option[ID]]] =
    db.run(deletedIO(id)).map {
      case 0 => Right(None)
      case _ =>
        Right(Some(id)) // again assumes that since id is the primary key, there will only be one
    }

}
