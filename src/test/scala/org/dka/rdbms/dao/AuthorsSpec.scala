package org.dka.rdbms.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.TearDownException
import org.dka.rdbms.dao.AuthorsSpec._
import org.dka.rdbms.model.Author
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Success, Try}

// todo:  how to seed data from scripts?
// todo: setup adds data and starts a transaction, teardown simply does a rollback???
// todo: teardown simply truncates table???  (only when tests are run sequentially!!)
// todo: use DBIO scripts in setup/tear down

class AuthorsSpec extends AnyFunSpec with DBTestRunner with Matchers {
  // for a test, this is fine ...
  implicit private val ec: ExecutionContext = ExecutionContext.global
  private val logger = Logger(getClass.getName)
  val delay: FiniteDuration = 10.seconds

  describe("populating") {
    it("should add an author") {
      val result = withDB(
        setup = noSetup,
        test = factory =>
          Try {
            Await.result(factory.authorsDao.insertAuthor(ja), delay) match {
              case Left(e) => fail(e)
              case Right(author) =>
                logger.debug(s"attempting to insert author.id: ${ja.id}")
                author.id shouldBe ja.id
            }
          },
        tearDown = factory => deleteAuthor(ja.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.value
    }

    it("should find a specific author") {
      val result = withDB(
        setup = factory => loadAuthor(jm)(factory, ec),
        test = factory =>
          Try {
            Await.result(factory.authorsDao.getAuthor(jm.id), delay) match {
              case Left(e) => fail(e)
              case Right(opt) => opt.fold(fail(s"did not find $jm"))(author => author shouldBe jm)
            }
          },
        tearDown = factory => deleteAuthor(jm.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.value
    }

  }

  private def loadAuthor(author: Author)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.authorsDao.insertAuthor(author), delay) match {
      case Left(e) => fail(e)
      case Right(_) => ()
    }
  }

  private def deleteAuthor(id: String)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.authorsDao.deleteAuthor(id), delay) match {
      case Left(e) => TearDownException(s"could not delete author $id", Some(e))
      case Right(idOpt) =>
        idOpt match {
          case None => TearDownException(s"did not find author $id to delete")
          case Some(deleted) =>
            logger.info(s"deleted author: $id")
            if (deleted == id) Success()
            else TearDownException(s"deleted wrong author, actual: $deleted, expected: $id")
        }
    }
  }
}

object AuthorsSpec {

  val jm: Author = Author("1", "Milton", "John", "555-123-4567", "Bread Street", "London", "UK", "12345")
  val ja: Author = Author("2", "Austen", "Jane", "555-234-5678", "11 Common Way", "Steventon", "UK", "23456")
  val cd: Author = Author("3", "Dickens", "Charles", "555-345-6789", "Landport", "Portsmouth", "UK", "34567")
  val mt: Author = Author("4", "Twain", "Mark", "555-456-7890", "Hannibal", "", "MO", "45678")

  val authors: Seq[Author] = Seq(jm, ja, cd, mt)
  val authorIds: Seq[String] = AuthorsSpec.authors.map(_.id)
}
