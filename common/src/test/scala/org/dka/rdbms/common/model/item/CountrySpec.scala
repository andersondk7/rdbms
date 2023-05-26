package org.dka.rdbms.common.model.item

import com.typesafe.scalalogging.Logger
import io.circe.parser.decode
import io.circe.syntax._
import org.dka.rdbms.common.model.fields._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class CountrySpec extends AnyFunSpec with Matchers {

  private val logger = Logger(getClass.getName)

  describe("read and write from json") {
    it("with all fields") {
      val country = Country(
        ID.build,
        Version.defaultVersion,
        CountryName.build("Far Far Away"),
        CountryAbbreviation.build("FFA"),
        CreateDate.now,
        UpdateDate.now
      )
      val json = country.asJson.noSpaces
      logger.debug(s"with all args: json: $json")
      decode[Country](json) match {
        case Left(error)    => fail(error)
        case Right(decoded) => decoded shouldBe country
      }
    }
  }

}
