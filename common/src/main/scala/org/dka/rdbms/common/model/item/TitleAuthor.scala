package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.components.{ID, Price, PublishDate, TitleName}
import org.dka.rdbms.common.model.validation.Validation._

final case class TitleAuthor(
  title: ID,
  Author: ID)
