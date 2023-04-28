package org.dka.rdbms.common.model

import io.circe.parser.decode
import io.circe.syntax._
import Publisher._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class PublisherSpec extends AnyFunSpec with Matchers {
  describe("read and write from json") {
    it("with all fields") {
      val publisher = Publisher(
        ID("1234"),
        CompanyName("Harper"),
        Some(Address("451 Main Street")),
        Some(City("Any Town")),
        Some(State("CA")),
        Some(Zip("12345-1234"))
      )
      val json = publisher.asJson.noSpaces
      println(s"with all args: json: $json")
      decode[Publisher](json) match {
        case Left(error) => fail(error)
        case Right(decoded) => decoded shouldBe publisher
      }
    }
    it("with optional fields") {
      val publisher = Publisher(
        ID("1234"),
        CompanyName("Harper"),
        None,
        None,
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
