package org.dka.rdbms.common.dao

import org.dka.rdbms.common.model.fields.ID
import org.dka.rdbms.common.model.item.Publisher

/**
 * adds methods beyond simple crud stuff anticipated to be mostly specific queries
 *
 * this interface is db agnostic and allows for easy unit testing since an database is not required
 */
trait PublisherDao extends CrudDao[Publisher] {}
