package org.dka.rdbms.anorm.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.dao.InvalidVersionException
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields._
import org.dka.rdbms.common.model.item.Book
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID
import scala.concurrent.duration.*
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Success, Try}

class BookDaoImplSpec extends AnyFunSpec with DBTestRunner with Matchers {
  import BookDaoImplSpec._
  // for a test, this is fine ...
  implicit private val ec: ExecutionContext = ExecutionContext.global
  private val logger = Logger(getClass.getName)
  val delay: FiniteDuration = 10.seconds

  describe("populating") {
    it("should add a book") {
      val result = withDB(
        setup = noSetup,
        test = factory =>
          Try {
            Await.result(factory.bookDao.create(pridePrejudice), delay) match {
              case Left(e) => fail(e)
              case Right(book) =>
                book.id shouldBe pridePrejudice.id
                book.version shouldBe pridePrejudice.version
                book.title shouldBe pridePrejudice.title
                book.price shouldBe pridePrejudice.price
                book.publisherID shouldBe pridePrejudice.publisherID
                book.publishDate shouldBe pridePrejudice.publishDate
                book.createDate shouldBe pridePrejudice.createDate
            }
          },
        tearDown = factory => deleteBook(pridePrejudice.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should find a specific book") {
      val result = withDB(
        setup = factory => loadBook(pridePrejudice)(factory, ec),
        test = factory =>
          Try {
            Await.result(factory.bookDao.read(pridePrejudice.id), delay) match {
              case Left(e) => fail(e.getMessage)
              case Right(opt) =>
                opt.fold(fail(s"did not find ${pridePrejudice.id}")) { book =>
                  logger.info(s"found: $book")
                  book.title shouldBe pridePrejudice.title
                }
            }
          },
        tearDown = factory => deleteBook(pridePrejudice.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
  }

  describe("updating") {
    it("should update once") {
      val updatedPrice = Price.build(BigDecimal(19.25))
      val result = withDB(
        setup = factory => loadBook(pridePrejudice)(factory, ec),
        test = factory =>
          Try {
            val updatedBook = pridePrejudice.copy(price = updatedPrice)
            Await.result(factory.bookDao.update(updatedBook)(ec), delay) match {
              case Left(e) => fail(e)
              case Right(updated) =>
                updated.version shouldBe updatedBook.version.next
                updated.createDate shouldBe updatedBook.createDate
                updated.title shouldBe updatedBook.title
                updated.price shouldBe updatedBook.price
                updated.lastUpdate should not be updatedBook.lastUpdate
            }
          },
        tearDown = factory => deleteBook(pridePrejudice.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
  }

  private def loadBook(book: Book)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.bookDao.create(book), delay) match {
      case Left(e) => fail(e)
      case Right(_) => ()
    }
  }

  private def deleteBook(id: ID)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.bookDao.delete(id), delay) match {
      case Left(e) => TearDownException(s"could not delete book $id", Some(e))
      case Right(idOpt) =>
        idOpt match {
          case None => TearDownException(s"did not find book $id to delete")
          case Some(deleted) =>
            logger.info(s"deleted publisher: $id")
            if (deleted == id) Success(())
            else TearDownException(s"deleted wrong book, actual: $deleted, expected: $id")
        }
    }
  }
}

object BookDaoImplSpec {

  val pridePrejudice: Book = Book(
    ID.build,
    Version.defaultVersion,
    Title.build("Pride and Prejudice"),
    Price.build(BigDecimal(12.34)),
    None, // publisherID
    None, // publishDate
    CreateDate.now,
    UpdateDate.now
  )

}
