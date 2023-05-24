package org.dka.rdbms.anorm.dao

import anorm.SimpleSql
import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.dao.*
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.ID
import anorm.*
import org.dka.rdbms.common.model.item.Updatable

import java.sql.Connection
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait CrudDaoImpl[T <: Updatable[T]] extends CrudDao[T] with DB {
  private val logger = Logger(getClass.getName)

  def create(item: T)(implicit ec: ExecutionContext): Future[DaoErrorsOr[T]] =
    Future {
      withConnection { implicit connection: Connection =>
        Try {
          val q = insertQ(item)
          q.execute()
        }.fold(
          ex => {
            logger.warn(s"could not insert $item, because $ex")
            Left(InsertException(s"could not insert $item", Some(ex)))
          },
          _ => Right(item)
        )
      }
    }

  def create(items: Seq[T])(implicit ec: ExecutionContext): Future[DaoErrorsOr[Int]] = Future {
    withConnection { _ =>
      Try {
        items.map(create)
      }.fold(
        ex => {
          logger.warn(s"could not insert ${items.size}, because $ex")
          Left(InsertException(s"could not insert ${items.size}", Some(ex)))
        },
        booleans =>
          if (booleans.contains(false)) Left(InsertException(s"could not insert all ${items.size}"))
          else Right(items.size)
      )
    }
  }

  def read(id: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Option[T]]] = Future {
    withConnection { implicit connection: Connection =>
      Try {
        val q: SimpleSql[Row] = byIdQ(id)
        val result: Option[T] = q.as(itemParser.singleOpt)
        result
      }.fold(
        ex => {
          logger.warn(s"count not read $id because $ex")
          Left(ItemNotFoundException(id))
        },
        result => Right(result)
      )
    }
  }

  def delete(id: ID)(implicit ec: ExecutionContext): Future[DaoErrorsOr[Option[ID]]] = Future {
    withConnection { implicit connection: Connection =>
      Try {
        val q = deleteQ(id)
        q.executeUpdate()
      }.fold(
        ex => {
          logger.warn(s"could not delete $id, because $ex")
          Left(DeleteException(s"could not delete $id", Some(ex)))
        },
        rowCount =>
          if (rowCount == 1) Right(Some(id))
          else Right(None)
      )
    }
  }

  def update(item: T)(implicit ec: ExecutionContext): Future[DaoErrorsOr[T]] = {
    implicit val connection: Connection = dataSource.getConnection
    connection.setAutoCommit(false)
    // these type of transaction boundary does not block until the row is released, it blows up
    // only these levels work:
    // Transaction_Repeatable_Read
    // Transaction_Serializable
    connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ)
    for {
      targetVersion <- checkVersion(item)
      result <- targetVersion match {
        case Left(ex) => Future.successful(Left(ex))
        case Right(_) => doUpdate(item)
      }
    } yield {
      connection.commit()
      connection.close()
      result
    }
  }

  protected def insertQ(t: T): SimpleSql[Row]
  protected def byIdQ(id: ID): SimpleSql[Row]
  protected def deleteQ(id: ID): SimpleSql[Row]
  protected def updateQ(item: T): SimpleSql[Row]
  protected def itemParser: RowParser[T]

  private def checkVersion(item: T)(implicit connection: Connection, ec: ExecutionContext): Future[DaoErrorsOr[T]] =
    Future {
      Try {
        val q: SimpleSql[Row] = byIdQ(item.id)
        val result: Option[T] = q.as(itemParser.singleOpt)
        result
      }.fold(
        ex => {
          logger.warn(s"count not read ${item.id} because $ex")
          Left(ItemNotFoundException(item.id))
        },
        {
          case None => Left(ItemNotFoundException(item.id))
          case Some(existing) =>
            if (existing.version == item.version) Right(item)
            else Left(InvalidVersionException(existing.version, Some(item.version)))
        }
      )
    }

  private def doUpdate(item: T)(implicit ec: ExecutionContext, connection: Connection): Future[DaoErrorsOr[T]] =
    Future {
      val updated = item.update
      Try {
        val q = updateQ(updated)
        q.executeUpdate()
      }.fold(
        ex => Left(UpdateException(updated.id, Some(ex))),
        count =>
          if (count == 1) Right(updated)
          else Left(UpdateException(updated.id))
      )
    }
}
