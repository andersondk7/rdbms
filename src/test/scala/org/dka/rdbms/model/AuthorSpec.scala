package org.dka.rdbms.model

import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import Author._

class AuthorSpec extends AnyFunSpec with Matchers {
  describe("read and write from json") {
    it("with all fields") {
      val author = Author(
        ID("1234"),
        LastName("Doe"),
        FirstName("John"),
        Some(Phone("123-555-1234")),
        Some(Address("451 Main Street")),
        Some(City("Any Town")),
        Some(State("CA")),
        Some(Zip("12345-1234"))
      )
      val json = author.asJson.noSpaces
      println(s"with all args: json: $json")
      decode[Author](json) match {
        case Left(error) => fail(error)
        case Right(decoded) => decoded shouldBe author
      }
    }
    it("with optional fields") {
      val author = Author(
        ID("1234"),
        LastName("Doe"),
        FirstName("John"),
        None,
        None,
        None,
        None,
        None
      )
      val json = author.asJson.noSpaces
      println(s"with missing args: json: $json")
      decode[Author](json) match {
        case Left(error) => fail(error)
        case Right(decoded) => decoded shouldBe author
      }
    }
  }
}
