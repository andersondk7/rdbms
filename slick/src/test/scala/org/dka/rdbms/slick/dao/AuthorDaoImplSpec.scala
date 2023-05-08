package org.dka.rdbms.slick.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.{TearDownException, TestResult}
import org.dka.rdbms.common.model.components.{FirstName, ID, LastName, LocationID}
import org.dka.rdbms.common.model.item
import org.dka.rdbms.common.model.item.Author
import org.dka.rdbms.slick.dao.AuthorDaoImplSpec._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class AuthorDaoImplSpec extends AnyFunSpec with DBTestRunner with Matchers {
  // for a test, this is fine ...
  implicit private val ec: ExecutionContext = ExecutionContext.global
  private val logger = Logger(getClass.getName)
  val delay: FiniteDuration = 10.seconds

  describe("conversion to/from db") {
    it("should convert from domain to db") {
      AuthorDaoImpl.toDB(mt) match {
        case None => fail(s"could not convert $mt")
        case Some((id, last, first, locationId)) =>
          id shouldBe mt.id.value.toString
          last shouldBe mt.lastName.value
          first shouldBe mt.firstName.map(_.value)
          locationId shouldBe mt.locationId.map(_.value.toString)
      }
    }
    it("should convert from db to domain") {
      val db = (
        mt.id.value.toString,
        mt.lastName.value,
        mt.firstName.map(_.value),
        mt.locationId.map(_.value.toString)
      )
      val converted = AuthorDaoImpl.fromDB(db)
      converted shouldBe mt
    }
  }

  describe("populating") {
    it("should add an author") {
      val result = withDB(
        setup = noSetup,
        test = factory =>
          Try {
            Await.result(factory.authorsDao.create(ja), delay) match {
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
      result.testResult.evaluate
    }
    it("should add multiple authors") {
      val result = withDB(
        setup = noSetup,
        test = factory =>
          Try {
            val added = Future
              .sequence(multipleAuthors.map(id => factory.authorsDao.create(id)))
              .map(_.partitionMap(identity))
            val (errors, _) = Await.result(added, delay)
            if (errors.nonEmpty) throw errors.head
            // todo, need to be able to combine errors...
            else succeed
          },
        tearDown = factory => {
          val deleted = Future
            .sequence(multipleAuthors.map(author => factory.authorsDao.delete(author.id)))
            .map(_.partitionMap(identity))
          val (errors, _) = Await.result(deleted, delay)
          if (errors.nonEmpty) throw errors.head
          // todo, need to be able to combine errors...
          else Success()
        }
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure match {
        case Some(t) => t.printStackTrace()
        case None => println(s"no failures here")
      }
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should find a specific author") {
      val result = withDB(
        setup = factory => loadAuthor(eh)(factory, ec),
        test = factory =>
          Try {
            Await.result(factory.authorsDao.read(eh.id), delay) match {
              case Left(e) => fail(e)
              case Right(opt) => opt.fold(fail(s"did not find $eh"))(author => author shouldBe eh)
            }
          },
        tearDown = factory => deleteAuthor(eh.id)(factory, ec)
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
            val response = Await.result(factory.authorsDao.getAuthorsForTitle(ID.build(bookId)), delay)
            val x = response match {
              case Left(e) =>
                fail(e)
              case Right(summaries) =>
                println(s"summaries: \n${summaries.mkString("\n")}")

                summaries.length shouldBe 2 // jacob and wilhelm
                val wilhelm = summaries.head
                val jacob = summaries.tail.head
                wilhelm.titleName.value shouldBe titleName
                jacob.titleName.value shouldBe titleName
            }
            x
//        },
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
  private def loadAuthor(author: Author)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.authorsDao.create(author), delay) match {
      case Left(e) => fail(e)
      case Right(_) => ()
    }
  }

  private def deleteAuthor(id: ID)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    logger.info(s"deleteAuthor: $id")
    logger.info(s"factory: $factory")
    Await.result(factory.authorsDao.delete(id), delay) match {
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

object AuthorDaoImplSpec {

  val jm: Author = item.Author(
    ID.build,
    LastName.build("Milton"),
    Some(FirstName.build("John")),
    None
  )
  val ja: Author = item.Author(
    ID.build,
    LastName.build("Austen"),
    Some(FirstName.build("Jane")),
    None
  )
  val cd: Author = item.Author(
    ID.build,
    LastName.build("Dickens"),
    Some(FirstName.build("Charles")),
    None
  )
  val mt: Author = item.Author(
    ID.build,
    LastName.build("Twain"),
    Some(FirstName.build("Mark")),
    None
  )
  val eh: Author = item.Author(
    ID.build,
    LastName.build("Hemmingway"),
    None,
    None
  )

  val multipleAuthors: Seq[Author] = Seq(ja, jm, cd, mt)
  val authorIds: Seq[ID] = AuthorDaoImplSpec.multipleAuthors.map(_.id)
}
