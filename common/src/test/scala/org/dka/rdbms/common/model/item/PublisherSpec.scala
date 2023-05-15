package org.dka.rdbms.common.model.item

import io.circe.parser.decode
import io.circe.syntax._
import org.dka.rdbms.common.model.fields._
import org.dka.rdbms.common.model.item
import org.dka.rdbms.common.model.item.Publisher._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class PublisherSpec extends AnyFunSpec with Matchers {
  describe("read and write from json") {
    it("with all fields") {
      val publisher = Publisher(
        ID.build,
        Version.defaultVersion,
        PublisherName.build("Harper"),
        Some(LocationID.build),
        Some(WebSite.build("http://somehere.com"))
      )
      val json = publisher.asJson.noSpaces
      println(s"with all args: json: $json")
      decode[Publisher](json) match {
        case Left(error) => fail(error)
        case Right(decoded) => decoded shouldBe publisher
      }
    }
    it("with optional fields") {
      val publisher = item.Publisher(
        ID.build,
        Version.defaultVersion,
        PublisherName.build("Harper"),
        None,
        None
      )
      val json = publisher.asJson.noSpaces
      println(s"with missing args: json: $json")
      decode[Publisher](json) match {
        case Left(error) => fail(error)
        case Right(decoded) => decoded shouldBe publisher
      }
    }
  }
}
