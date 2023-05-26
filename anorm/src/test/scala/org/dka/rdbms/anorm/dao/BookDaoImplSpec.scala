package org.dka.rdbms.anorm.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.dao.InvalidVersionException
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.*
import org.dka.rdbms.common.model.item.Book
import org.dka.rdbms.db.load.Generator
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

  private val logger                        = Logger(getClass.getName)

  val delay: FiniteDuration                 = 10.seconds

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

  describe("bookDao methods") {
    //
    // currently there needs to be seed data in the test db as found in
    // db/src/main/resources/db/seed/local
    //  I don't like this and the tests should all be modified
    //  so that they create all the data they need and do not
    //  depend on any 'seed' data
    //  this is a todo task
    //
    it("should get all ids") {
      val result = withDB(
        setup = noSetup, // just read the books that are already in the db
        test = factory =>
          Try {
            implicit val ctx: ExecutionContext = ec
            Await.result(factory.bookDao.getAllIds, delay) match {
              case Left(e) => fail(e)
              case Right(ids) =>
                logger.info(s"found ${ids.size}")
                succeed

            }
          },
        tearDown = noSetup
      )
    }
    it("should find authors for a given book") {
      val bookId    = "e1de1c95-19e5-4df6-aa49-7c1f7b1d1868"
      val titleName = "Grimms Fairy Tales"
      val result = withDB(
        noSetup,
        test = factory =>
          Try {
            val response = Await.result(factory.bookDao.getBookAuthorSummary(ID.build(bookId)), delay)
            response match {
              case Left(e) =>
                fail(e)
              case Right(summaries) =>
                logger.debug(s"summaries: \n${summaries.mkString("\n")}")

                summaries.length shouldBe 2 // jacob and wilhelm
                val wilhelm = summaries.head
                val jacob   = summaries.tail.head
                wilhelm.titleName.value shouldBe titleName
                wilhelm.authorOrder shouldBe 2
                jacob.titleName.value shouldBe titleName
                jacob.authorOrder shouldBe 1
            }
          }.recoverWith { case t: Throwable =>
            logger.debug(s"caught $t")
            // t.printStackTrace()
            fail(t)
          },
        noSetup
      )
      logger.debug(s"setup:  ${result.setupResult}")
      result.setupGood shouldBe true
      result.tearDownGood shouldBe true
      result.testResult.evaluate
    }
  }

  private def loadBook(book: Book)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.bookDao.create(book), delay) match {
      case Left(e)  => fail(e)
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
            logger.info(s"deleted book: $id")
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

  def genBook: Book = new Book(
    id = ID.build,
    version = Version.defaultVersion,
    title = Title.build(Generator.genString(Title.maxLength)),
    price = Price.build(Generator.genPrice),
    None, // publisherID
    None, // publisherDate,
    createDate = CreateDate.now
  )

}
