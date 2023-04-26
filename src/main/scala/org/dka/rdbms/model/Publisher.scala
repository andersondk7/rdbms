package org.dka.rdbms.model

final case class Publisher(
  id: ID,
  name: String,
  address: String,
  city: String,
  state: String,
  zip: String
                    )
