package org.dka.rdbms.common.model.item

import io.circe.parser.decode
import io.circe.syntax._
import org.dka.rdbms.common.model.components._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class CountrySpec extends AnyFunSpec with Matchers {
  describe("read and write from json") {
    it("with all fields") {
      val country = Country(
        ID.build,
        CountryName.build("Far Far Away"),
        CountryAbbreviation.build("FFA")
      )
      val json = country.asJson.noSpaces
      println(s"with all args: json: $json")
      decode[Country](json) match {
        case Left(error) => fail(error)
        case Right(decoded) => decoded shouldBe country
      }
    }
  }
}
