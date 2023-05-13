package org.dka.rdbms.slick.config

import com.typesafe.config.ConfigFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import pureconfig._
import pureconfig.generic.auto._
import DBConfig._ // must be kept even though intellij thinks it is unused

class ConfigSpec extends AnyFunSpec with Matchers {
  describe("reading config") {
    it("should read from dev by default") {
      ConfigSource
        .fromConfig(
          ConfigFactory.load().getConfig("DBConfig") // just want this piece of the config file
        )
        .load[DBConfig] match {
        case Left(errors) => fail(s"could not read because $errors")
        case Right(config) =>
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
    }
  }
}
