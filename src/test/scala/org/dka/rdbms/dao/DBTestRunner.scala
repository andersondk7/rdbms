package org.dka.rdbms.dao

import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}
import com.typesafe.scalalogging.Logger
import org.dka.rdbms.{TestRunner, TestRunnerResult}

import scala.util.{Failure, Success, Try}

trait DBTestRunner extends TestRunner[DaoFactory] {

  private val factoryBuilder = DaoFactoryBuilder.configure

  val noSetup: DaoFactory => Try[Unit] = _ => Success()

  /**
   * Runs test using a DaoFactory
   */
  def withDB(
    setup: DaoFactory => Try[Unit],
    test: DaoFactory => Try[Assertion],
    tearDown: DaoFactory => Try[Unit]
  ): TestRunnerResult = factoryBuilder match {
    case Left(e) => fail(e)
    case Right(factory) =>
      runWithFixture(factory, setup, test, tearDown)
  }

}
