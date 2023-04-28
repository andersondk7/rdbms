package org.dka.rdbms.slick.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.TearDownException
import org.dka.rdbms.common.model.{Address, City, CompanyName, ID, Publisher, State, Zip}
import PublisherDaoImplSpec._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Success, Try}

class PublisherDaoImplSpec extends AnyFunSpec with DBTestRunner with Matchers {
  // for a test, this is fine ...
  implicit private val ec: ExecutionContext = ExecutionContext.global
  private val logger = Logger(getClass.getName)
  val delay: FiniteDuration = 10.seconds

  describe("populating") {
    it("should add a publisher") {
      val result = withDB(
        setup = noSetup,
        test = factory =>
          Try {
            Await.result(factory.publisherDao.create(rh), delay) match {
              case Left(e) => fail(e)
              case Right(publisher) =>
                logger.debug(s"attempting to insert author.id: ${rh.id}")
                publisher.id shouldBe rh.id
            }
          },
        tearDown = factory => deletePublisher(rh.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.value
    }
    it("should add multiple authors") {
      val result = withDB(
        setup = noSetup,
        test = factory =>
          Try {
            val added = Future
              .sequence(multiplePublishers.map(id => factory.publisherDao.create(id)))
              .map(_.partitionMap(identity))
            val (errors, _) = Await.result(added, delay)
            if (errors.nonEmpty) throw errors.head
            // todo, need to be able to combine errors...
            else succeed
          },
        tearDown = factory => {
          val deleted = Future
            .sequence(multiplePublishers.map(publisher => factory.publisherDao.delete(publisher.id)))
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
      result.testResult.value
    }

    it("should find a specific author") {
      val result = withDB(
        setup = factory => loadPublisher(ad)(factory, ec),
        test = factory =>
          Try {
            Await.result(factory.publisherDao.read(ad.id), delay) match {
              case Left(e) => fail(e)
              case Right(opt) => opt.fold(fail(s"did not find $ad"))(publisher => publisher shouldBe ad)
            }
          },
        tearDown = factory => deletePublisher(ad.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.value
    }
  }

  describe("conversion to/from db") {
    it("should convert from domain to db") {
      PublisherDaoImpl.toDB(rh) match {
        case None => fail(s"could not convert $rh")
        case Some((id, name, address, city, state, zip)) =>
          id shouldBe rh.id.value
          name shouldBe rh.name.value
          address shouldBe rh.address.map(_.value)
          city shouldBe rh.city.map(_.value)
          state shouldBe rh.state.map(_.value)
          zip shouldBe rh.zip.map(_.value)
      }
    }
    it("should convert from db to domain") {
      val db = (
        rh.id.value,
        rh.name.value,
        rh.address.map(_.value),
        rh.city.map(_.value),
        rh.state.map(_.value),
        rh.zip.map(_.value)
      )
      val converted = PublisherDaoImpl.fromDB(db)
      converted shouldBe rh
    }
  }

  private def loadPublisher(publisher: Publisher)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.publisherDao.create(publisher), delay) match {
      case Left(e) => fail(e)
      case Right(_) => ()
    }
  }

  private def deletePublisher(id: ID)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    logger.info(s"deletePublisher: $id")
    logger.info(s"factory: $factory")
    Await.result(factory.publisherDao.delete(id), delay) match {
      case Left(e) => TearDownException(s"could not delete publisher $id", Some(e))
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

object PublisherDaoImplSpec {

  val rh: Publisher = Publisher(
    ID("1"),
    CompanyName("RandomHouse"),
    Some(Address("1745 Broadway")),
    None,
    Some(State("NY")),
    Some(Zip("10019"))
  )
  val hb: Publisher = Publisher(
    ID("2"),
    CompanyName("Hachette Book Group"),
    Some(Address("1290 Sixth Ave.")),
    Some(City("New York")),
    Some(State("NY")),
    Some(Zip("10104"))
  )
  val hc: Publisher = Publisher(
    ID("3"),
    CompanyName("Harper Collins"),
    Some(Address("195 Broadway")),
    Some(City("New York")),
    Some(State("NY")),
    Some(Zip("10007"))
  )
  val ad: Publisher = Publisher(
    ID("4"),
    CompanyName("Addison-Wesley"),
    Some(Address("1900 East Lake Avenue")),
    Some(City("Glenview")),
    Some(State("IL")),
    Some(Zip("60025"))
  )

  val multiplePublishers: Seq[Publisher] = Seq(hb, hc, ad)
  val publisherIds: Seq[ID] = PublisherDaoImplSpec.multiplePublishers.map(_.id)
}
