package org.dka.rdbms.common.model.item

import cats.data.Validated._
import cats.implicits._
import io.circe._
import org.dka.rdbms.common.model.Validation._
import org.dka.rdbms.common.model.{ID, Price, PublishDate, TitleName}

final case class TitleAuthor(
                        title: ID,
                        Author: ID
                      )