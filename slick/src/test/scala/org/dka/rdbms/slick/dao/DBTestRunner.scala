package org.dka.rdbms.slick.dao

import cats.data.Validated._
import org.dka.rdbms.{TestRunner, TestRunnerResult}
import org.dka.rdbms.common.config.ConfigException
import org.dka.rdbms.common.config.DBConfig.ConfigErrorsOr
import org.scalatest.Assertion
import org.scalatest.Assertions.fail

import scala.util.{Success, Try}

trait DBTestRunner extends TestRunner[DaoFactory] {

  private val factoryBuilder: ConfigErrorsOr[DaoFactory] = DaoFactory.configure

  val noSetup: DaoFactory => Try[Unit] = _ => Success()

  /**
   * Runs test using a DaoFactory
   */
  def withDB(
    setup: DaoFactory => Try[Unit],
    test: DaoFactory => Try[Assertion],
    tearDown: DaoFactory => Try[Unit]
  ): TestRunnerResult = factoryBuilder match {
    case Invalid(chain) =>
      val reasons = ConfigException.reasons(chain).mkString(" : ")
      fail(new IllegalStateException(reasons))
    case Valid(factory) => runWithFixture(factory, setup, test, tearDown)
  }

}
