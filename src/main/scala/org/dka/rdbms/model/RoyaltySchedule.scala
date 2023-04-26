package org.dka.rdbms.model

final case class RoyaltySchedule (
  titleId: String,
  lowRange: Int,
  highRange: Int,
  royalty: BigDecimal
                                   )
