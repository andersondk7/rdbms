package org.dka.rdbms.slick

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.dao.BookDao
import org.dka.rdbms.common.model.fields.ID
import org.dka.rdbms.common.model.query.BookAuthorSummary
import org.dka.rdbms.slick.dao.{DaoFactory, DaoFactoryBuilder}
import org.scalatest.{Outcome, ScalaTestVersion}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class JoinPerformanceItSpec extends AnyFunSpec with Matchers {

  import JoinPerformanceItSpec._

  private val factoryBuilder = DaoFactoryBuilder.configure
  def withFactory(testCode: DaoFactory => Any): Unit =
    factoryBuilder match {
      case Left(error) => fail(error)
      case Right(factory) => testCode(factory)
    }

  describe("join testing") {
    it("getAuthorsForBooks single query") {
      ""
      withFactory { factory =>
        val ids: Seq[ID] = Await.result(factory.bookDao.getAllIds, delay)
          .getOrElse(fail("could not get ids"))
        val id: ID = ids.head
        val now = System.currentTimeMillis()
        // make a call for each book (all 2000 of them)
        Await.result(factory.bookDao.getAuthorsForBook(id), delay).getOrElse(throw new Exception("error"))
        val time = System.currentTimeMillis() - now
        println(s"single query, time: $time")
      }
    }
    it("getAuthorsForBooks concurrently") {
      ""
      withFactory { factory =>
        val ids: Seq[ID] = Await.result(factory.bookDao.getAllIds, delay)
          .getOrElse(fail("could not get ids"))
        ids.size shouldBe bookCount
        val now = System.currentTimeMillis()
        // 2000 concurrent queries
        // make a call for each book (all 2000 of them)
        val queries: Future[Seq[BookAuthorSummary]] = Future.sequence(ids.map(id => {
          factory.bookDao.getAuthorsForBook(id).map(_.getOrElse(throw new Exception(s"failed reading bookDao for $id")))
        })).map(_.flatten)
        val summaries: Seq[BookAuthorSummary] = Await.result(queries, delay)
        val time = System.currentTimeMillis() - now
        println(s"concurrent for ${ids.size} queries, time: $time, avg time: ${time / ids.size}")
        summaries.size shouldBe (bookCount * authorsPerBook)
      }
    }
    it("getAuthorsForBooks sequentially") {
      ""
      withFactory { factory =>
        val ids: Seq[ID] = Await.result(factory.bookDao.getAllIds, delay)
          .getOrElse(fail("could not get ids"))
        ids.size shouldBe bookCount
        val now = System.currentTimeMillis()
        // sequential queries
        val query: (ID, BookDao) => Seq[BookAuthorSummary] = (id, dao) =>
          Await.result(dao.getAuthorsForBook(id), delay).getOrElse(throw new Exception(s"could not get summary for $id"))
        val summaries: Seq[BookAuthorSummary] = ids.flatMap(query(_, factory.bookDao))
        val time = System.currentTimeMillis() - now
//        println(s"summaries.size: ${summaries.size}") // load has 4 authors per book
        println(s"sequential for ${ids.size} queries, time: $time, avg time: ${time / ids.size}")
        summaries.size shouldBe (bookCount * authorsPerBook)
      }
    }
  }
}

object JoinPerformanceItSpec {
  val delay: FiniteDuration = 30.seconds
  val bookCount = 2000 // based on the load scripts
  val authorsPerBook = 4 // based on the load scripts
}
