package org.dka.rdbms

import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}
import com.typesafe.scalalogging.Logger

import scala.util.{Success, Try}

/**
 * Mixin trait to run tests that require setting up and tearing down around tests
 */

trait TestRunner[F] {
  def withFixture(
    fixture: F,
    setup: F => Try[Assertion],
    test: F => Try[Assertion],
    tearDown: F => Assertion
  ): Assertion = try {
    val result = for {
      _ <- setup(fixture)
      assertion <- test(fixture)
    } yield assertion
    println(s"result: $result")
    result.get
  } finally tearDown(fixture)
}
