package org.dka.rdbms.slick.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.dao._
import org.dka.rdbms.common.model.fields.ID
import org.dka.rdbms.common.model.item.Updatable
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Implementation of a CrudDao using an instance of the database
 * @tparam D
 *   domain object stored
 */
trait CrudDaoImpl[D <: Updatable[D]] extends CrudDao[D] {
  def db: Database
  private val logger = Logger(getClass.getName)

  //
  // crud IO operations
  //
  protected def singleCreateIO: D => DBIO[Int]
  protected def multipleCreateIO: Seq[D] => DBIO[Option[Int]]
  protected def getIO: (ID, ExecutionContext) => DBIO[Option[D]]
  protected def deletedIO: ID => DBIO[Int]
  protected val updateAction: (D, ExecutionContext) => DBIO[D]

  override def create(item: D)(implicit ec: ExecutionContext): Future[DaoErrorsOr[D]] =
    db.run(singleCreateIO(item))
      .map { c: Int =>
        if (c == 1) Right(item)
        else throw InsertException(s"${item.getClass.getName}: could not insert $item")
      }

  override def create(items: Seq[D])(implicit ec: ExecutionContext): Future[DaoErrorsOr[Int]] =
    db.run(multipleCreateIO(items)).map {
      case Some(count) => Right(count)
      case None =>
        throw InsertException(s"${items.getClass.getName} could not insert ${items.size}")
    }

  override def read(id: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Option[D]]] =
    // note this only gets the first, assume that since id is the primary key, there will only be one!
    db.run(getIO(id, ec)).map(r => Right(r)) // don't know how to capture when it fails...

  override def delete(id: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Option[ID]]] =
    db.run(deletedIO(id)).map {
      case 0 => Right(None)
      case _ =>
        Right(Some(id)) // again assumes that since id is the primary key, there will only be one
    }

  override def update(item: D)(implicit ec: ExecutionContext): Future[DaoErrorsOr[D]] = {
    val getItemAction = getIO(item.id, ec).map {
      case None => throw ItemNotFoundException(item.id)
      case Some(i) if i.version == item.version =>
        println(s"update same version: ${i.version.value}, ${item.version.value}")
        i
      case Some(i) =>
        println(s"update different version: ${i.version.value}, ${item.version.value}")
        throw InvalidVersionException(item.version, i.version)
    }

    val combo = (getItemAction andThen updateAction(item, ec))
      .transactionally

    db.run(combo)
      .map(Right(_))
      .recoverWith {
        case de: DaoException => Future(Left(de))
      }
  }
}
