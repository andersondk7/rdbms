package org.dka.rdbms.model

// see https://docs.scala-lang.org/overviews/core/value-classes.html
final case class AuthorId(value: String) extends AnyVal

final case class Author(
  id: String,
  lastName: String,
  firstName: String,
  phone: String,
  address: String,
  city: String,
  state: String,
  zip: String) { }
