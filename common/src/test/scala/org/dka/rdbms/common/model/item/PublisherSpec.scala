package org.dka.rdbms.common.model.item

import io.circe.parser.decode
import io.circe.syntax._
import org.dka.rdbms.common.model.components._
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
        CompanyName.build("Harper"),
        Some(Address.build("451 Main Street")),
        Some(City.build("Any Town")),
        Some(State.build("CA")),
        Some(Zip.build("12345-1234"))
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
        CompanyName.build("Harper"),
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
