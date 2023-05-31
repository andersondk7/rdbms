package org.dka.rdbms.zio.api

import zio.ZIO
import zio.test.*
import zio.test.Assertion.*
import zio.http.*


object AppSpec extends ZIOSpecDefault {

    def spec: Spec[Any, Option[Nothing]] = suite("http")(
    test("should get author") {
      val authorQuery = App.authors
      val request = Request.default(Method.GET, URL(Root / "query" / "author" / "42"))
      val result = for {
        body <- authorQuery.runZIO(request).map(_.body)
      } yield {
        body
      }
      val expected: Body = Body.fromString("author(42)")
      assertZIO(result)(equalTo(expected))
    }
  )
}
