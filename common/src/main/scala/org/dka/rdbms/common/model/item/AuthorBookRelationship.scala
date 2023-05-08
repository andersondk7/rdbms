package org.dka.rdbms.common.model.item

import org.dka.rdbms.common.model.fields.ID

final case class AuthorBookRelationship(authorId: ID, bookId: ID, authorOrder: Int) {}
