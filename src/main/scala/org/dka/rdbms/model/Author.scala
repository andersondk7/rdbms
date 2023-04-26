package org.dka.rdbms.model


final case class Author(
                         id: ID,
                         lastName: LastName,
                         firstName: FirstName,
                         phone: Option[Phone],
                         address: Option[Address],
                         city: Option[City],
                         state: Option[State],
                         zip: Option[Zip]) { }
