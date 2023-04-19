package org.dka.rdbms.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.dao.AuthorsSpec._
import org.dka.rdbms.model.Author
import org.scalatest.Assertion
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

// todo:  how to seed data from scripts?
// todo: setup adds data and starts a transaction, teardown simply does a rollback???
// todo: teardown simply truncates table???  (only when tests are run sequentially!!)
// todo: use DBIO scripts in setup/tear down

class AuthorsSpec extends AnyFunSpec with DBTestRunner with Matchers {
  // for a test, this is fine ...
  implicit private val ec: ExecutionContext = ExecutionContext.global
  private val logger = Logger("AuthorsSpec")
  val delay: FiniteDuration = 10.seconds

  describe("populating") {
    it("should add an author") {
      withDB(
        setup = noSetup,
        test = factory =>
          Try {
            Await.result(factory.authorsDao.insertAuthor(ja), delay) match {
              case Left(e) => fail(e)
              case Right(author) =>
                logger.debug(s"attempting to insert ${ja.id}")
                author.id shouldBe ja.id
            }
          },
        tearDown = factory => deleteAuthor(ja.id)(factory, ec).get
      )
    }
    it("should find a specific author") {
      val x: String = "42"
      withDB(
        setup = factory => loadAuthor(jm)(factory, ec),
        test = factory => Try {
          Await.result(factory.authorsDao.getAuthor(jm.id), delay) match {
            case Left(e) => fail(e)
            case Right(opt) => opt.fold(fail(s"did not find $jm"))(author => author shouldBe jm)
          }
        },
        tearDown = factory => deleteAuthor(jm.id)(factory, ec).get
      )
    }
  }

  private def loadAuthor(author: Author)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Assertion] = Try {
    Await.result(factory.authorsDao.insertAuthor(author), delay) match {
      case Left(e) => fail(e)
      case Right(_) => succeed
    }
  }

  private def deleteAuthor(id: String)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Assertion] = {
    Await.result(factory.authorsDao.deleteAuthor(id), delay) match {
      case Left(e) => fail(e)
      case Right(idOpt) => idOpt match {
        case None => Failure(fail(s"did not delete $id"))
        case Some(deleted) => Success(deleted shouldBe id)
      }
    }
  }

//  private def deleteAuthors(ids: Seq[String])(implicit factory: DaoFactory, ec: ExecutionContext): Try[Assertion] = {
//    Await.result(factory.authorsDao.deleteAuthor(ids.head), delay) match {
//      case Left(e) => fail(e)
//      case Right(idOpt) => idOpt match {
//        case None => Failure(fail(s"did not delete $id"))
//        case Some(deleted) => Success(deleted shouldBe id)
//      }
//    }
//  }
}

object AuthorsSpec {

  val jm: Author = Author("1", "Milton", "John", "555-123-4567", "Bread Street", "London", "UK", "12345")
  val ja: Author = Author("2", "Austen", "Jane", "555-234-5678", "11 Common Way", "Steventon", "UK", "23456")
  val cd: Author = Author("3", "Dickens", "Charles", "555-345-6789", "Landport", "Portsmouth", "UK", "34567")
  val mt: Author = Author("4", "Twain", "Mark", "555-456-7890", "Hannibal", "", "MO", "45678")

  val authors: Seq[Author] = Seq(jm, ja, cd, mt)
  val authorIds: Seq[String] = AuthorsSpec.authors.map(_.id)
}
