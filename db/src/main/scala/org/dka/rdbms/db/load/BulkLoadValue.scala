package org.dka.rdbms.db.load

import org.dka.rdbms.common.model.item.Country

trait BulkLoad[I] {
  def header: String
  def insertLine(i: I): String
}

object BulkLoadValueInstances {
  implicit val bulkLoadCountry: BulkLoad[Country] = new BulkLoad[Country] {
    override def header: String = "insert into countries(id, country_name, country_abbreviation)\n  values"

    override def insertLine(c: Country): String =
      s"('${c.id.value.toString}', ${c.countryName.value}', ${c.countryAbbreviation.value})"
  }
}

object BulkLoadSyntax {
  implicit class BulkLoadOps[I](value: I) {
    def header(implicit instance: BulkLoad[I]): String = instance.header
    def insertLine(implicit instance: BulkLoad[I]): String = instance.insertLine(value)
  }
}
