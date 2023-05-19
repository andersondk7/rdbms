package org.dka.rdbms.common.config

import cats.data.Validated._
import org.dka.rdbms.common.config.DBConfig.ConfigErrorsOr
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ConfigSpec extends AnyFunSpec with Matchers {
  describe("reading config") {
    it("should read from dev by default") {
      DBConfig.load
      match {
        case Invalid(nec) => fail(nec.toString)
        case Valid(config) =>
          println(s"config: $config")
        config.connectionPool shouldBe "HikariCP"
        config.dataSourceClass shouldBe "org.postgresql.ds.PGSimpleDataSource"
        config.properties.host shouldBe "localhost"
        config.properties.port shouldBe 5432
        config.numThreads shouldBe 10
        config.maxConnections shouldBe 10
        config.queueSize shouldBe 10000
        // skip the user and password part since they will be different depending on who runs the test
        config.url.split("user").head shouldBe "jdbc:postgresql://localhost:5432/book_biz?"
      }

      succeed
//        case Left(errors) => fail(s"could not read because $errors")
//        case Right(config) =>
//      }
    }
  }
}
