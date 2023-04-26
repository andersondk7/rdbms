package org.dka.rdbms.model

final case class Publisher(
  id: ID,
  name: CompanyName,
  address: Address,
  city: City,
  state: State,
  zip: Zip
                    )
