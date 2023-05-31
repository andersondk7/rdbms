package org.dka.rdbms.zio.api

import com.typesafe.scalalogging.Logger
import zio.*
import zio.http.*

object App extends ZIOAppDefault {
  private val logger = Logger(getClass.getName)

  private val port =9090

  val health: HttpApp[Any, Response] = Http.collect[Request] {
    case req @ Method.GET -> Root / "heartBeat" =>
      logger.info(s"request method: ${req.method} -- url: ${req.url} -- path: ${req.path}")
      Response.text(s"${java.lang.System.currentTimeMillis}")
  }

  val authors:  Http[Any, Nothing, Request, Response] = Http.collectZIO[Request] {
      case Method.GET -> Root / "query" / "author" / id  => ZIO.succeed(Response.text(s"author($id)"))
    }

  private val app = health ++ authors

  override val run: ZIO[Any, Throwable, Nothing] =
    Server.serve(app).provide(Server.defaultWithPort(port))
}
