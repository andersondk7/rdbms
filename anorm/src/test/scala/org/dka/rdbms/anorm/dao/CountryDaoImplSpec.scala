package org.dka.rdbms.anorm.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.dao.InvalidVersionException
import org.dka.rdbms.common.dao.Validation.DaoErrorsOr
import org.dka.rdbms.common.model.fields.{CountryAbbreviation, CountryName, CreateDate, ID, UpdateDate, Version}
import org.dka.rdbms.common.model.item.Country
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID
import scala.concurrent.duration.*
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Success, Try}

class CountryDaoImplSpec extends AnyFunSpec with DBTestRunner with Matchers {

  import CountryDaoImplSpec._

  // for a test, this is fine ...
  implicit private val ec: ExecutionContext = ExecutionContext.global

  private val logger                        = Logger(getClass.getName)

  val delay: FiniteDuration                 = 10.seconds

  describe("populating") {
    it("should add a country") {
      val result = withDB(
        setup = noSetup,
        test = factory =>
          Try {
            Await.result(factory.countryDao.create(iceland), delay) match {
              case Left(e) => fail(e)
              case Right(country) =>
                country.id shouldBe iceland.id
                country.version shouldBe iceland.version
                country.countryName shouldBe iceland.countryName
                country.countryAbbreviation shouldBe iceland.countryAbbreviation
            }
          },
        tearDown = factory => deleteCountry(iceland.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should add multiple countries") {
      val countries = List(
        Country(
          ID.build,
          Version.defaultVersion,
          CountryName.build("Norway"),
          CountryAbbreviation.build("NRW"),
          CreateDate.now,
          UpdateDate.now
        ),
        Country(
          ID.build,
          Version.defaultVersion,
          CountryName.build("Sweden"),
          CountryAbbreviation.build("SWE"),
          CreateDate.now,
          UpdateDate.now
        ),
        Country(
          ID.build,
          Version.defaultVersion,
          CountryName.build("Finland"),
          CountryAbbreviation.build("FNLD"),
          CreateDate.now,
          UpdateDate.now
        ),
        Country(
          ID.build,
          Version.defaultVersion,
          CountryName.build("Denmark"),
          CountryAbbreviation.build("DMK"),
          CreateDate.now,
          UpdateDate.now
        )
      )
      val countryIds = countries.map(_.id)

      val result = withDB(
        setup = noSetup,
        test = factory =>
          Try {
            Await.result(factory.countryDao.create(countries), delay) match {
              case Left(e) => fail(e)
              case Right(_) =>
                val found =
                  countryIds.flatMap(id => Await.result(factory.countryDao.read(id)(ec), delay).getOrElse(None)).size
                println(s"found: $found")
                found shouldBe countryIds.size

            }
          },
        tearDown = factory => Try(countryIds.foreach(id => deleteCountry(id)(factory, ec)))
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should find a specific country") {
      val result = withDB(
        setup = factory => loadCountry(iceland)(factory, ec),
        test = factory =>
          Try {
            Await.result(factory.countryDao.read(irelandId), delay) match {
              case Left(e) => fail(e.getMessage)
              case Right(opt) =>
                opt.fold(fail(s"did not find $irelandId")) { country =>
                  logger.info(s"found: $country")
                  country.countryName shouldBe CountryName.build("Ireland")
                }
            }
          },
        tearDown = factory => deleteCountry(iceland.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
  }

  describe("updating") {
    it("should update once") {
      val updatedAbbreviation = "ILD"
      val result = withDB(
        setup = factory => loadCountry(iceland)(factory, ec),
        test = factory =>
          Try {
            val updatedCountry = iceland.copy(countryAbbreviation = CountryAbbreviation.build(updatedAbbreviation))
            Await.result(factory.countryDao.update(updatedCountry)(ec), delay) match {
              case Left(e) => fail(e)
              case Right(updated) =>
                updated.version shouldBe updatedCountry.version.next
                updated.createDate shouldBe updatedCountry.createDate
                updated.countryName shouldBe updatedCountry.countryName
                updated.countryAbbreviation shouldBe updatedCountry.countryAbbreviation
                updated.lastUpdate should not be updatedCountry.lastUpdate
            }
          },
        tearDown = factory => deleteCountry(iceland.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should fail update with old version (sequentially)") {
      /*
      scenario:
      - bill wants to update only the country name
      - susan wants to update only the country abbreviation
      - neither bill nor susan is aware of the other's edits
      result:
      - the first one to make an update (countryName) succeeds
      - the second one to make an update (countryAbbreviation) fails
       */
      val updatedCountryName         = "FrostyLand"
      val updatedCountryAbbreviation = "FTLD"
      val result = withDB(
        setup = factory => loadCountry(iceland)(factory, ec),
        test = factory =>
          Try {
            val firstChange  = iceland.copy(countryName = CountryName.build(updatedCountryName))
            val secondChange = iceland.copy(countryAbbreviation = CountryAbbreviation.build(updatedCountryAbbreviation))
            Await.result(factory.countryDao.update(firstChange)(ec), delay) match {
              case Left(e) => fail(s"firstChange failed with", e)
              case Right(updated) =>
                updated.version shouldBe firstChange.version.next
                updated.createDate shouldBe firstChange.createDate
                updated.countryName shouldBe firstChange.countryName
                updated.countryAbbreviation shouldBe firstChange.countryAbbreviation
                updated.lastUpdate should not be firstChange.lastUpdate
            }
            Await.result(factory.countryDao.update(secondChange)(ec), delay) match {
              case Left(e) =>
                logger.info(s"failed with $e")
                e shouldBe a[InvalidVersionException]
              case Right(updated) =>
                logger.debug(s"after second change: $updated")
                fail(s"second change ($secondChange) with old version succeeded")
            }
          },
        tearDown = factory => deleteCountry(iceland.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
    it("should fail update with old version (async)") {
      /*
      scenario:
      - bill wants to update only the country name
      - susan wants to update only the country abbreviation
      - neither bill nor susan is aware of the other's edits
      result:
      - the first one to make an update (countryName) succeeds
      - the second one to make an update (countryAbbreviation) fails
       */
      val updatedCountryName         = "FrostyLand"
      val updatedCountryAbbreviation = "FTLD"
      val result = withDB(
        setup = factory => loadCountry(iceland)(factory, ec),
        test = factory =>
          Try {
            val firstChange  = iceland.copy(countryName = CountryName.build(updatedCountryName))
            val secondChange = iceland.copy(countryAbbreviation = CountryAbbreviation.build(updatedCountryAbbreviation))
            // launch async
            val attempt1 = factory.countryDao.update(firstChange)(ec)
            val attempt2 = factory.countryDao.update(secondChange)(ec)

            val finished = Await.result(Future.sequence(Seq(attempt1, attempt2)), delay)
            val countries = finished.collect { case Right(country) =>
              country
            }
            val errors = finished.collect { case Left(e) =>
              e
            }
            countries.size shouldBe 1
            errors.size shouldBe 1
          },
        tearDown = factory => deleteCountry(iceland.id)(factory, ec)
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
  }

  private def loadCountry(country: Country)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.countryDao.create(country), delay) match {
      case Left(e)  => fail(e)
      case Right(_) => ()
    }
  }

  private def deleteCountry(id: ID)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.countryDao.delete(id), delay) match {
      case Left(e) => TearDownException(s"could not delete country $id", Some(e))
      case Right(idOpt) =>
        idOpt match {
          case None => TearDownException(s"did not find country $id to delete")
          case Some(deleted) =>
            logger.info(s"deleted country: $id")
            if (deleted == id) Success(())
            else TearDownException(s"deleted wrong country, actual: $deleted, expected: $id")
        }
    }
  }

}

object CountryDaoImplSpec {

  val irelandId: ID = ID.build("52e8b846-a068-4847-8223-b156c356a70a")

  val iceland: Country = Country(
    ID.build,
    Version.defaultVersion,
    CountryName.build("Iceland"),
    CountryAbbreviation.build("ICL"),
    CreateDate.now,
    UpdateDate.now
  )

}
