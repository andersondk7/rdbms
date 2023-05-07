package org.dka.rdbms.common.model.query

import io.circe._
import org.dka.rdbms.common.model.components.{FirstName, LastName, TitleName}

final case class TitleAuthorSummary(
                                   titleName: TitleName,
                                   authorLastName: LastName,
                                   authorFirstName: Option[FirstName]
                                   )

object TitleAuthorSummary {
  def apply(title: String, lastName: String, firstName: Option[String]): TitleAuthorSummary =
    new TitleAuthorSummary(
      TitleName.build(title),
      LastName.build(lastName),
      FirstName.build(firstName)
    )

  implicit val encodeTitleAuthorSummary: Encoder[TitleAuthorSummary] = (summary: TitleAuthorSummary) => {
    val objects = List(
      Some(TitleName.toJson(summary.titleName)),
      Some(LastName.toJson(summary.authorLastName)),
      FirstName.toJson(summary.authorFirstName)
    ).flatten // filter out the None, i.e. only needed lines
    Json.obj(objects: _*)
  }
}
