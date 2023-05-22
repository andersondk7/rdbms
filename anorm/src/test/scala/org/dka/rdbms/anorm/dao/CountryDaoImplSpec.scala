package org.dka.rdbms.anorm.dao

import com.typesafe.scalalogging.Logger
import org.dka.rdbms.common.dao.InvalidVersionException
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
  private val logger = Logger(getClass.getName)
  val delay: FiniteDuration = 10.seconds

  describe("populating") {
    it("should find a specific country") {
      val result = withDB(
        setup = noSetup,
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
        tearDown = noSetup
      )
      result.setupResult.failure shouldBe None
      result.tearDownResult.failure shouldBe None
      result.testResult.evaluate
    }
  }
  private def loadCountry(country: Country)(implicit factory: DaoFactory, ec: ExecutionContext): Try[Unit] = Try {
    Await.result(factory.countryDao.create(country), delay) match {
      case Left(e) => fail(e)
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
