package org.dka.rdbms.common.model.item

import org.dka.rdbms.common.model.components.ID

final case class AuthorTitleRelationship(authorId: ID, titleId: ID, authorOrder: Int) {}
