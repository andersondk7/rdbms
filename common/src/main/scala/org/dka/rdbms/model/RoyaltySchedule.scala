package org.dka.rdbms.model

import io.circe._

final case class RoyaltySchedule(
  titleId: String,
  lowRange: Int,
  highRange: Int,
  royalty: BigDecimal)
