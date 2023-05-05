package org.dka.rdbms.common.model.item

import io.circe.parser.decode
import io.circe.syntax._
import org.dka.rdbms.common.model.components._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.util.UUID

class TitleSpec extends AnyFunSpec with Matchers {
  describe("read and write from json") {
    it("with all fields") {
      val title = Title(
        ID.build,
        TitleName.build("Some Epic Title"),
        Price.build(BigDecimal(98.34)),
        Some(PublisherID.build(UUID.randomUUID())),
        Some(PublishDate.build(LocalDate.now()))
      )
      val json = title.asJson.noSpaces
      println(s"title: $title")
      println(s"with all fields json: $json")
      decode[Title](json) match {
        case Left(error) => fail(error)
        case Right(decoded) => decoded shouldBe title
      }
    }
    it("with optional fields") {
      val title = Title(
        ID.build,
        TitleName.build("Some Epic Title"),
        Price.build(BigDecimal(98.34)),
        None,
        None
      )
      val json = title.asJson.noSpaces
      decode[Title](json) match {
        case Left(error) => fail(error)
        case Right(decoded) => decoded shouldBe title
      }
    }
  }
  describe("with valid json. but holding invalid model") {
    it("should fail when model errors") {
      // title name is too short
      // price is zero
      val json = s""" {"ID":"1234","title":"","price":"0.00"} """
      decode[Title](json) match {
        case Left(error) =>
          println(s"title model errors: $error")
          succeed
        case Right(_) => fail(s"should not have parsed")
      }
    }
    it("should fail when missing required fields") {
      // missing price
      val json = s""" {"ID":"1234","title":"some epic novel"} """
      decode[Title](json) match {
        case Left(error) =>
          println(s"title missing field errors: $error")
        case Right(_) => fail(s"should not have parsed")
      }
    }
    it("should fail with invalid json and domain errors") {
      val json = s""" {"ID":"1234","title":"some epic novel", "price": "76,21", "publisher" "1234"} """
      decode[Title](json) match {
        case Left(error) =>
          println(s"title invalid json errors: $error")
          succeed
        case Right(_) => fail(s"should not have parsed")
      }
    }
  }
}
