package org.dka.rdbms.common.model

import io.circe._

final case class RoyaltySchedule(
  titleId: String,
  lowRange: Int,
  highRange: Int,
  royalty: BigDecimal)
