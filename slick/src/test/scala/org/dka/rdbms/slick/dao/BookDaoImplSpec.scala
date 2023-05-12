package org.dka.rdbms.slick.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.TearDownException
import org.dka.rdbms.common.model.fields.{ID, Price, Title}
import org.dka.rdbms.common.model.item.Book
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Success, Try}

class BookDaoImplSpec extends AnyFunSpec with DBTestRunner with Matchers {
  import BookDaoImplSpec._
  // for a test, this is fine ...
  implicit private val ec: ExecutionContext = ExecutionContext.global
  private val logger = Logger(getClass.getName)
  val delay: FiniteDuration = 10.seconds

  describe("conversion to/from db") {
    it("should convert from domain to db") {
      BookDaoImpl.toDB(PandP) match {
        case None => fail(s"could not convert $PandP")
        case Some((id, title, price, _, _)) =>
          id shouldBe PandP.id.value.toString
          title shouldBe PandP.title.value
          price shouldBe PandP.price.value
      }
    }
    it("should convert from db to domain") {
      val db = (
        PandP.id.value.toString,
        PandP.title.value,
        PandP.price.value,
        None,
        None
      )
      val converted = BookDaoImpl.fromDB(db)
      converted shouldBe PandP
    }
  }

  describe("populating") {
    it("should add an Book") {
      val result = withDB(
        setup = noSetup,
        test = factory =>
          Try {
            Await.result(factory.bookDao.create(PandP), delay) match {
              case Left(e) => fail(e)
              case Right(book) =>
                logger.debug(s"attempting to insert book.id: ${PandP.id}")
                book.id shouldBe PandP.id
            }
          },
        tearDown = factory => deleteBook(PandP.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should find a specific book") {
      val result = withDB(
        setup = factory => loadBook(PandP)(factory, ec),
        test = factory =>
          Try {
            Await.result(factory.bookDao.read(PandP.id), delay) match {
              case Left(e) => fail(e)
              case Right(opt) => opt.fold(fail(s"did not find $PandP"))(bookd => bookd shouldBe PandP)
            }
          },
        tearDown = factory => deleteBook(PandP.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
  }

  describe("queries") {
    it("should find authors for a given book") {
      val bookId = "e1de1c95-19e5-4df6-aa49-7c1f7b1d1868"
      val titleName = "Grimms Fairy Tales"
      val result = withDB(
        noSetup,
        test = factory =>
          Try {
            val response = Await.result(factory.bookDao.getAuthorsForBook(ID.build(bookId)), delay)
            response match {
              case Left(e) =>
                fail(e)
              case Right(summaries) =>
                println(s"summaries: \n${summaries.mkString("\n")}")

                summaries.length shouldBe 2 // jacob and wilhelm
                val wilhelm = summaries.head
                val jacob = summaries.tail.head
                wilhelm.titleName.value shouldBe titleName
                wilhelm.authorOrder shouldBe 2
                jacob.titleName.value shouldBe titleName
                jacob.authorOrder shouldBe 1
            }
          }.recoverWith { case t: Throwable =>
            println(s"caught $t")
            t.printStackTrace()
            fail(t)
          },
        noSetup
      )
      println(s"setup:  ${result.setupResult}")
      result.setupGood shouldBe true
      result.tearDownGood shouldBe true
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
    logger.info(s"deleteBook: $id")
    logger.info(s"factory: $factory")
    Await.result(factory.bookDao.delete(id), delay) match {
      case Left(e) => TearDownException(s"could not delete book $id", Some(e))
      case Right(idOpt) =>
        idOpt match {
          case None => TearDownException(s"did not find book $id to delete")
          case Some(deleted) =>
            logger.info(s"deleted book: $id")
            if (deleted == id) Success()
            else TearDownException(s"deleted wrong book, actual: $deleted, expected: $id")
        }
    }
  }
}

object BookDaoImplSpec {

  val PandP: Book = Book(
    ID.build,
    Title.build("Pride and Prejudice"),
    Price.build(BigDecimal(12.34)),
    None,
    None
  )
}
