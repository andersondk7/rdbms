package org.dka.rdbms.dao

import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}
import com.typesafe.scalalogging.Logger
import org.dka.rdbms.TestRunner

import scala.util.{Failure, Success, Try}

trait DBTestRunner extends TestRunner[DaoFactory] {
  /*
  val noSetup: DaoFactory => Try[Assertion] = _ => Success(succeed)
  val failSetup: DaoFactory => Try[Assertion] = _ => Failure(new Exception("expected exception in setup"))

  val noTearDown: DaoFactory => Try[Assertion] = _ => Success(succeed)

  private val factoryBuilder = DaoFactoryBuilder.configure

  /**
   * Runs test using a DaoFactory
   */
  def withDB(
    setup: DaoFactory => Try[Assertion],
    test: DaoFactory => Try[Assertion],
    tearDown: DaoFactory => Assertion
  ): Assertion = factoryBuilder match {
    case Left(e) => fail(e)
    case Right(factory) =>
      runWithFixture(factory, setup, test, tearDown)
  }

   */
}
