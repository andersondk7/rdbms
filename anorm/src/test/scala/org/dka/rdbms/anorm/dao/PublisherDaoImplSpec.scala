package org.dka.rdbms.anorm.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.dao.InvalidVersionException
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields._
import org.dka.rdbms.common.model.item.Publisher
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID
import scala.concurrent.duration.*
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Success, Try}

class PublisherDaoImplSpec extends AnyFunSpec with DBTestRunner with Matchers {

  import PublisherDaoImplSpec._

  // for a test, this is fine ...
  implicit private val ec: ExecutionContext = ExecutionContext.global

  private val logger                        = Logger(getClass.getName)

  val delay: FiniteDuration                 = 10.seconds

  describe("populating") {
    it("should add a publisher w/o website") {
      val result = withDB(
        setup = noSetup,
        test = factory =>
          Try {
            Await.result(factory.publisherDao.create(hb), delay) match {
              case Left(e) => fail(e)
              case Right(publisher) =>
                publisher.id shouldBe hb.id
                publisher.version shouldBe hb.version
                publisher.publisherName shouldBe hb.publisherName
                publisher.locationId shouldBe hb.locationId
                publisher.webSite shouldBe hb.webSite
            }
          },
        tearDown = factory => deletePublisher(hb.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should add a publisher with website") {
      val result = withDB(
        setup = noSetup,
        test = factory =>
          Try {
            Await.result(factory.publisherDao.create(rh), delay) match {
              case Left(e) => fail(e)
              case Right(publisher) =>
                publisher.id shouldBe rh.id
                publisher.version shouldBe rh.version
                publisher.publisherName shouldBe rh.publisherName
                publisher.locationId shouldBe rh.locationId
                publisher.webSite shouldBe rh.webSite
            }
          },
        tearDown = factory => deletePublisher(rh.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should find a specific publisher") {
      val result = withDB(
        setup = factory => loadPublisher(rh)(factory, ec),
        test = factory =>
          Try {
            Await.result(factory.publisherDao.read(rh.id), delay) match {
              case Left(e) => fail(e.getMessage)
              case Right(opt) =>
                opt.fold(fail(s"did not find ${rh.id}")) { publisher =>
                  logger.info(s"found: $publisher")
                  publisher.publisherName shouldBe rh.publisherName
                }
            }
          },
        tearDown = factory => deletePublisher(rh.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
  }

  describe("updating") {
    it("should update once") {
      val updatedName = PublisherName.build("RandomName")
      val result = withDB(
        setup = factory => loadPublisher(rh)(factory, ec),
        test = factory =>
          Try {
            val updatedPublisher = rh.copy(publisherName = updatedName)
            Await.result(factory.publisherDao.update(updatedPublisher)(ec), delay) match {
              case Left(e) => fail(e)
              case Right(updated) =>
                updated.version shouldBe updatedPublisher.version.next
                updated.createDate shouldBe updatedPublisher.createDate
                updated.publisherName shouldBe updatedName
                updated.webSite shouldBe rh.webSite
                updated.lastUpdate should not be updatedPublisher.lastUpdate
            }
          },
        tearDown = factory => deletePublisher(rh.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
  }

  private def loadPublisher(publisher: Publisher)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.publisherDao.create(publisher), delay) match {
      case Left(e)  => fail(e)
      case Right(_) => ()
    }
  }

  private def deletePublisher(id: ID)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.publisherDao.delete(id), delay) match {
      case Left(e) => TearDownException(s"could not delete publisher $id", Some(e))
      case Right(idOpt) =>
        idOpt match {
          case None => TearDownException(s"did not find publisher $id to delete")
          case Some(deleted) =>
            logger.info(s"deleted publisher: $id")
            if (deleted == id) Success(())
            else TearDownException(s"deleted wrong publisher, actual: $deleted, expected: $id")
        }
    }
  }

}

object PublisherDaoImplSpec {

  val rh: Publisher = Publisher(
    ID.build,
    Version.defaultVersion,
    PublisherName.build("RandomHouse"),
    None,
    Some(WebSite.build("www.random.com"))
  )

  val hb: Publisher = Publisher(
    ID.build,
    Version.defaultVersion,
    PublisherName.build("Hachette Book Group"),
    None,
    None
  )

}
