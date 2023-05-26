package org.dka.rdbms.common.model.item

import com.typesafe.scalalogging.Logger
import io.circe.parser.decode
import io.circe.syntax._
import org.dka.rdbms.common.model.fields._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.util.UUID

class BookSpec extends AnyFunSpec with Matchers {

  private val logger = Logger(getClass.getName)

  describe("read and write from json") {
    it("with all fields") {
      val title = Book(
        ID.build,
        Version.defaultVersion,
        Title.build("Some Epic Book"),
        Price.build(BigDecimal(98.34)),
        Some(PublisherID.build(UUID.randomUUID())),
        Some(PublishDate.build(LocalDate.now())),
        CreateDate.now,
        UpdateDate.now
      )
      val json = title.asJson.noSpaces
      logger.debug(s"title: $title")
      logger.debug(s"with all fields json: $json")
      decode[Book](json) match {
        case Left(error)    => fail(error)
        case Right(decoded) => decoded shouldBe title
      }
    }
    it("with optional fields") {
      val title = Book(
        ID.build,
        Version.defaultVersion,
        Title.build("Some Epic Book"),
        Price.build(BigDecimal(98.34)),
        None,
        None
      )
      val json = title.asJson.noSpaces
      decode[Book](json) match {
        case Left(error)    => fail(error)
        case Right(decoded) => decoded shouldBe title
      }
    }
  }

  describe("with valid json. but holding invalid model") {
    it("should fail when model errors") {
      // title name is too short
      // price is zero
      val json = s""" {"ID":"1234","title":"","price":"0.00"} """
      decode[Book](json) match {
        case Left(error) =>
          logger.debug(s"title model errors: $error")
          succeed
        case Right(_) => fail(s"should not have parsed")
      }
    }
    it("should fail when missing required fields") {
      // missing price
      val json = s""" {"ID":"1234","title":"some epic novel"} """
      decode[Book](json) match {
        case Left(error) =>
          logger.debug(s"title missing field errors: $error")
        case Right(_) => fail(s"should not have parsed")
      }
    }
    it("should fail with invalid json and domain errors") {
      val json = s""" {"ID":"1234","title":"some epic novel", "price": "76,21", "publisher" "1234"} """
      decode[Book](json) match {
        case Left(error) =>
          logger.debug(s"title invalid json errors: $error")
          succeed
        case Right(_) => fail(s"should not have parsed")
      }
    }
  }

}
