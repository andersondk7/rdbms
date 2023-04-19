package org.dka.rdbms.dao

import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}
import com.typesafe.scalalogging.Logger
import org.dka.rdbms.TestRunner

import scala.util.{Success, Try}

trait DBTestRunner extends TestRunner[DaoFactory] {
  val noSetup: DaoFactory => Try[Assertion] = _ => Success(succeed)
  val noTearDown: DaoFactory => Try[Assertion] = _ => Success(succeed)

  private val factoryBuilder = DaoFactoryBuilder.configure

  /**
   * Runs test in a controlled manner:
   *
   * @param setup
   *   function that sets up the test
   * @param test
   *   the actual test function -- only runs if setup returns Success
   * @param tearDown
   *   function to clean up -- '''always runs'''
   * @return
   */
  def withDB(
    setup: DaoFactory => Try[Assertion],
    test: DaoFactory => Try[Assertion],
    tearDown: DaoFactory => Assertion
  ): Assertion = factoryBuilder match {
    case Left(e) => fail(e)
    case Right(factory) =>
      withFixture(factory, setup, test, tearDown)
  }
}
