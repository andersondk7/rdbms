package org.dka.rdbms.anorm.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.dao.InvalidVersionException
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.*
import org.dka.rdbms.common.model.item.Location
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID
import scala.concurrent.duration.*
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Success, Try}

class LocationDaoImplSpec extends AnyFunSpec with DBTestRunner with Matchers {
  import LocationDaoImplSpec._
  // for a test, this is fine ...
  implicit private val ec: ExecutionContext = ExecutionContext.global
  private val logger = Logger(getClass.getName)
  val delay: FiniteDuration = 10.seconds

  describe("populating") {
    it("should add a location") {
      val result = withDB(
        setup = noSetup,
        test = factory =>
          Try {
            Await.result(factory.locationDao.create(bamberg), delay) match {
              case Left(e) => fail(e)
              case Right(location) =>
                location.id shouldBe bamberg.id
                location.version shouldBe bamberg.version
                location.locationName shouldBe bamberg.locationName
                location.locationAbbreviation shouldBe bamberg.locationAbbreviation
            }
          },
        tearDown = factory => deleteLocation(bamberg.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should find a specific location") {
      val result = withDB(
        setup = factory => loadLocation(bamberg)(factory, ec),
        test = factory =>
          Try {
            Await.result(factory.locationDao.read(bamberg.id), delay) match {
              case Left(e) => fail(e.getMessage)
              case Right(opt) =>
                opt.fold(fail(s"did not find $bamberg")) { location =>
                  logger.info(s"found: $location")
                  location.locationName shouldBe bamberg.locationName
                }
            }
          },
        tearDown = factory => deleteLocation(bamberg.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
  }

  describe("updating") {
    it("should update once") {
      val updatedAbbreviation = "BAM"
      val result = withDB(
        setup = factory => loadLocation(bamberg)(factory, ec),
        test = factory =>
          Try {
            val updatedLocation = bamberg.copy(locationAbbreviation = LocationAbbreviation.build(updatedAbbreviation))
            Await.result(factory.locationDao.update(updatedLocation)(ec), delay) match {
              case Left(e) => fail(e)
              case Right(updated) =>
                updated.version shouldBe updatedLocation.version.next
                updated.createDate shouldBe updatedLocation.createDate
                updated.locationName shouldBe updatedLocation.locationName
                updated.locationAbbreviation shouldBe updatedLocation.locationAbbreviation
                updated.lastUpdate should not be updatedLocation.lastUpdate
            }
          },
        tearDown = factory => deleteLocation(bamberg.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should fail update with old version (sequentially)") {
      /*
      scenario:
      - bill wants to update only the location name
      - susan wants to update only the location abbreviation
      - neither bill nor susan is aware of the other's edits
      result:
      - the first one to make an update (locationName) succeeds
      - the second one to make an update (locationAbbreviation) fails
       */
      val updatedLocationName = "BammyBerg"
      val updatedLocationAbbreviation = "BMBRG"
      val result = withDB(
        setup = factory => loadLocation(bamberg)(factory, ec),
        test = factory =>
          Try {
            val firstChange = bamberg.copy(locationName = LocationName.build(updatedLocationName))
            val secondChange =
              bamberg.copy(locationAbbreviation = LocationAbbreviation.build(updatedLocationAbbreviation))
            Await.result(factory.locationDao.update(firstChange)(ec), delay) match {
              case Left(e) => fail(s"firstChange failed with", e)
              case Right(updated) =>
                updated.version shouldBe firstChange.version.next
                updated.createDate shouldBe firstChange.createDate
                updated.locationName shouldBe firstChange.locationName
                updated.locationAbbreviation shouldBe firstChange.locationAbbreviation
                updated.lastUpdate should not be firstChange.lastUpdate
            }
            Await.result(factory.locationDao.update(secondChange)(ec), delay) match {
              case Left(e) =>
                logger.info(s"failed with $e")
                e shouldBe a[InvalidVersionException]
              case Right(updated) =>
                logger.debug(s"after second change: $updated")
                fail(s"second change ($secondChange) with old version succeeded")
            }
          },
        tearDown = factory => deleteLocation(bamberg.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should fail update with old version (async)") {
      /*
      scenario:
      - bill wants to update only the location name
      - susan wants to update only the location abbreviation
      - neither bill nor susan is aware of the other's edits
      result:
      - the first one to make an update (locationName) succeeds
      - the second one to make an update (locationAbbreviation) fails
       */
      val updatedLocationName = "BammyBerg"
      val updatedLocationAbbreviation = "BMBRG"
      val result = withDB(
        setup = factory => loadLocation(bamberg)(factory, ec),
        test = factory =>
          Try {
            val firstChange = bamberg.copy(locationName = LocationName.build(updatedLocationName))
            val secondChange =
              bamberg.copy(locationAbbreviation = LocationAbbreviation.build(updatedLocationAbbreviation))
            // launch async
            val attempt1 = factory.locationDao.update(firstChange)(ec)
            val attempt2 = factory.locationDao.update(secondChange)(ec)

            val finished = Await.result(Future.sequence(Seq(attempt1, attempt2)), delay)
            val locations = finished.collect { case Right(country) =>
              country
            }
            val errors = finished.collect { case Left(e) =>
              e
            }
            locations.size shouldBe 1
            errors.size shouldBe 1
          },
        tearDown = factory => deleteLocation(bamberg.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
  }

  private def loadLocation(location: Location)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.locationDao.create(location), delay) match {
      case Left(e) => fail(e)
      case Right(_) => ()
    }
  }

  private def deleteLocation(id: ID)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.locationDao.delete(id), delay) match {
      case Left(e) => TearDownException(s"could not delete location $id", Some(e))
      case Right(idOpt) =>
        idOpt match {
          case None => TearDownException(s"did not find location $id to delete")
          case Some(deleted) =>
            logger.info(s"deleted location: $id")
            if (deleted == id) Success(())
            else TearDownException(s"deleted wrong location, actual: $deleted, expected: $id")
        }
    }
  }
}

object LocationDaoImplSpec {
  val bamberg: Location = Location(
    ID.build,
    Version.defaultVersion,
    LocationName.build("Bamberg"),
    LocationAbbreviation.build("BA"),
    CountryID.build(UUID.fromString("b6dee7ce-663e-4dd4-bdd3-4ed55a014467")),
    CreateDate.now
  )
  val bambergId: ID = ID.build("52e8b846-a068-4847-8223-b156c356a70a")
}
