package org.dka.rdbms.common.model.fields

/**
 * Container for a value of type T
 * @tparam T
 */
trait Field[T] {

  def value: T

}
