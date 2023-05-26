package org.dka.rdbms.anorm


import cats.data.Validated._
import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.config._
import org.dka.rdbms.common.dao.BookDao
import org.dka.rdbms.common.model.fields.ID
import org.dka.rdbms.common.model.query.BookAuthorSummary
import org.dka.rdbms.anorm.dao.{BookDaoImpl, DaoFactory}
import org.scalatest.{Outcome, ScalaTestVersion}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class JoinPerformanceItSpec extends AnyFunSpec with Matchers {
  private val logger = Logger(getClass.getName)

  import JoinPerformanceItSpec._

  describe("join testing: slick") {
    it("getAuthorsForBooks single query, first") {
      withFactory { factory =>
        val ids: Seq[ID] = getIds(factory.bookDao)
        val id: ID = ids.head
        val now = System.currentTimeMillis()
        // make a call for each book (all 2000 of them)
        Await.result(factory.bookDao.getBookAuthorSummary(id), delay).getOrElse(throw new Exception("error"))
        val time = System.currentTimeMillis() - now
        logger.info(s"slick: first single query, time: $time")
      }
    }
    it("getAuthorsForBooks concurrently") {
      withFactory { factory =>
        val ids: Seq[ID] = getIds(factory.bookDao)
        ids.size shouldBe bookCount
        val now = System.currentTimeMillis()
        // 2000 concurrent queries
        // make a call for each book (all 2000 of them)
        val queries: Future[Seq[BookAuthorSummary]] = Future
          .sequence(ids.map { id =>
            factory.bookDao
              .getBookAuthorSummary(id)
              .map(_.getOrElse(throw new Exception(s"failed reading bookDao for $id")))
          })
          .map(_.flatten)
        val summaries: Seq[BookAuthorSummary] = Await.result(queries, delay)
        val time = System.currentTimeMillis() - now
        logger.info(s"slick: concurrent for ${ids.size} queries, time: $time, avg time: ${time / ids.size}")
        summaries.size shouldBe (bookCount * authorsPerBook)
      }
    }
    it("getAuthorsForBooks sequentially") {
      withFactory { factory =>
        val ids: Seq[ID] = getIds(factory.bookDao)
        ids.size shouldBe bookCount
        val now = System.currentTimeMillis()
        // sequential queries
        val query: (ID, BookDao) => Seq[BookAuthorSummary] = (id, dao) =>
          Await
            .result(dao.getBookAuthorSummary(id), delay)
            .getOrElse(throw new Exception(s"could not get summary for $id"))
        val summaries: Seq[BookAuthorSummary] = ids.flatMap(query(_, factory.bookDao))
        val time = System.currentTimeMillis() - now
        logger.info(s"slick: sequential for ${ids.size} queries, time: $time, avg time: ${time / ids.size}")
        summaries.size shouldBe (bookCount * authorsPerBook)
      }
    }
    it("getAuthorsForBooks single query, last") {
      ""
      withFactory { factory =>
        val ids: Seq[ID] = getIds(factory.bookDao)
        val id: ID = ids.head
        val now = System.currentTimeMillis()
        // make a call for each book (all 2000 of them)
        Await.result(factory.bookDao.getBookAuthorSummary(id), delay).getOrElse(throw new Exception("error"))
        val time = System.currentTimeMillis() - now
        logger.info(s"slick: last single query, time: $time")
      }
    }
  }

  private val configure = DaoFactory.configure

  private def withFactory(testCode: DaoFactory => Any): Unit =
    configure match {
      case Invalid(chain) =>
        val reasons = ConfigException.reasons(chain).mkString(" : ")
        fail(new IllegalStateException(reasons))
      case Valid(factory) => testCode(factory)
    }

  private def getIds(bookDao: BookDao): Seq[ID] =
    Await
      .result(bookDao.getAllIds, delay)
      .getOrElse(fail("could not get ids"))
}

object JoinPerformanceItSpec {
  val delay: FiniteDuration = 30.seconds
  val bookCount = 2000 // based on the load scripts
  val authorsPerBook = 4 // based on the load scripts
}
