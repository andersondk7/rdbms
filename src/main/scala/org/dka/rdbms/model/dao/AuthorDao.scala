package org.dka.rdbms.model.dao

import org.dka.rdbms.model.{Author, ID}

/**
 * adds methods beyond simple crud stuff anticipated to be mostly specific queries
 *
 * this interface is db agnostic and allows for easy unit testing since an database is not required
 */
trait AuthorDao extends CrudDao[Author, ID] {}
