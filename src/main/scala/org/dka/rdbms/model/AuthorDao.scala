package org.dka.rdbms.model

/**
 * adds methods beyond simple crud stuff
 *
 * this interface is db agnostic and allows for easy unit testing since an database is not required
 */
trait AuthorDao extends CrudDao[Author, String]{
}

