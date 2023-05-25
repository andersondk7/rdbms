package org.dka.rdbms.anorm.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.dao.InvalidVersionException
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields._
import org.dka.rdbms.common.model.item.Author
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID
import scala.concurrent.duration.*
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Success, Try}

class AuthorDaoImplSpec extends AnyFunSpec with DBTestRunner with Matchers {
  import AuthorDaoImplSpec._
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
            Await.result(factory.authorDao.create(jm), delay) match {
              case Left(e) => fail(e)
              case Right(author) =>
                author.id shouldBe jm.id
                author.version shouldBe jm.version
                author.firstName shouldBe jm.firstName
                author.lastName shouldBe jm.lastName
                author.locationId shouldBe jm.locationId
            }
          },
        tearDown = factory => deleteAuthor(jm.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should find a specific author") {
      val result = withDB(
        setup = factory => loadAuthor(jm)(factory, ec),
        test = factory =>
          Try {
            Await.result(factory.authorDao.read(jm.id), delay) match {
              case Left(e) => fail(e.getMessage)
              case Right(opt) =>
                opt.fold(fail(s"did not find ${jm.id}")) { author =>
                  logger.info(s"found: $author")
                  author.lastName shouldBe jm.lastName
                }
            }
          },
        tearDown = factory => deleteAuthor(jm.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
  }

  describe("updating") {
    it("should update once") {
      val updatedName = LastName.build("Jimmy")
      val result = withDB(
        setup = factory => loadAuthor(jm)(factory, ec),
        test = factory =>
          Try {
            val updatedAuthor = jm.copy(lastName = updatedName)
            Await.result(factory.authorDao.update(updatedAuthor)(ec), delay) match {
              case Left(e) => fail(e)
              case Right(updated) =>
                updated.version shouldBe updatedAuthor.version.next
                updated.createDate shouldBe updatedAuthor.createDate
                updated.lastName shouldBe updatedName
                updated.lastUpdate should not be updatedAuthor.lastUpdate
            }
          },
        tearDown = factory => deleteAuthor(jm.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
  }

  private def loadAuthor(author: Author)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.authorDao.create(author), delay) match {
      case Left(e) => fail(e)
      case Right(_) => ()
    }
  }

  private def deleteAuthor(id: ID)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.authorDao.delete(id), delay) match {
      case Left(e) => TearDownException(s"could not delete author $id", Some(e))
      case Right(idOpt) =>
        idOpt match {
          case None => TearDownException(s"did not find author $id to delete")
          case Some(deleted) =>
            logger.info(s"deleted author: $id")
            if (deleted == id) Success(())
            else TearDownException(s"deleted wrong author, actual: $deleted, expected: $id")
        }
    }
  }
}

object AuthorDaoImplSpec {

  val jm: Author = Author(
    ID.build,
    Version.defaultVersion,
    LastName.build("Milton"),
    Some(FirstName.build("John")),
    None // locationId
  )
}
