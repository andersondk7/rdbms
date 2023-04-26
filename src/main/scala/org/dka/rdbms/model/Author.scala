package org.dka.rdbms.model


final case class Author(
                         id: ID,
                         lastName: LastName,
                         firstName: FirstName,
                         phone: Phone,
                         address: Address,
                         city: City,
                         state: State,
                         zip: Zip) { }
