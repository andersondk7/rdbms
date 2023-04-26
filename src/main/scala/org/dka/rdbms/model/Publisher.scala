package org.dka.rdbms.model

final case class Publisher(
  id: ID,
  name: CompanyName,
  address: Option[Address],
  city: Option[City],
  state: Option[State],
  zip: Option[Zip]
                    )
