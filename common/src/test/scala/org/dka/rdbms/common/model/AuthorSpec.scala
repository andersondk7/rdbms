package org.dka.rdbms.common.model

import io.circe.syntax._
import io.circe.parser.decode
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import Author._

class AuthorSpec extends AnyFunSpec with Matchers {
  describe("read and write from json") {
    it("with all fields") {
      val author = Author(
        ID.build("1234"),
        LastName.build("Doe"),
        FirstName.build("John"),
        Some(Phone.build("123-555-1234")),
        Some(Address.build("451 Main Street")),
        Some(City.build("Any Town")),
        Some(State.build("CA")),
        Some(Zip.build("12345-1234"))
      )
      val json = author.asJson.noSpaces
      decode[Author](json) match {
        case Left(error) => fail(error)
        case Right(decoded) => decoded shouldBe author
      }
    }
    it("with optional fields") {
      val author = Author(
        ID.build("1234"),
        LastName.build("Doe"),
        FirstName.build("John"),
        None,
        None,
        None,
        None,
        None
      )
      val json = author.asJson.noSpaces
      decode[Author](json) match {
        case Left(error) => fail(error)
        case Right(decoded) => decoded shouldBe author
      }
    }
  }
  describe("with valid json. but holding invalid model") {
    it("should fail when too short") {
      // first name is too short
      val json = s""" {"ID":"1234","lastName":"Doe","firstName":""} """
      decode[Author](json) match {
        case Left(error) => error.getMessage contains "firstName must be at least 1"
        case Right(_) => fail(s"should not have parsed")
      }
    }
    it("should fail when too long") {
      // first name can't be more that 20 chars
      val json = s""" {"ID":"1234","lastName":"Doe","firstName":"123456789 123456789 12345"} """
      decode[Author](json) match {
        case Left(error) => error.getMessage contains "firstName can't be longer than 20"
        case Right(_) => fail(s"should not have parsed")
      }
    }
    it("should fail when multiple domain errors") {
      // first name can't be more that 20 chars
      // lastName can't be empty
      val json = s""" {"ID":"1234","firstName":"123456789 123456789 12345", "lastName": ""} """
      decode[Author](json) match {
        case Left(error) =>
          error.getMessage contains "firstName can't be longer than 20"
          error.getMessage contains "lastName must be at least 1"
        case Right(_) => fail(s"should not have parsed")
      }
    }
    it("should fail when both json and domain errors") {
      // first name can't be more that 20 chars
      // lastName is missing from json
      val json = s""" {"ID":"1234","firstName":"123456789 123456789 12345", "last_name": ""} """
      decode[Author](json) match {
        case Left(error) =>
          error.getMessage contains "firstName can't be longer than 20"
          error.getMessage contains "lastName must be at least 1"
        case Right(_) => fail(s"should not have parsed")
      }
    }
    it("should fail with invalid json and domain errors") {
      // first name can't be more that 20 chars
      // lastName is missing from json
      val json = s""" {"ID":"1234","firstName":"123456789 123456789 12345", "last_name: ""} """
      decode[Author](json) match {
        case Left(error) => succeed
        case Right(_) => fail(s"should not have parsed")
      }
    }
  }
}
