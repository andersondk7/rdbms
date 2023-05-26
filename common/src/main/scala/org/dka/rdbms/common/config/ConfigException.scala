package org.dka.rdbms.common.config

import cats.data.NonEmptyChain

sealed trait ConfigException extends Throwable {

  val reason: String

  override def getMessage: String = reason

}

final case class MissingFieldException(fieldName: String) extends ConfigException {

  override val reason: String = s"missing field: $fieldName"

}

case class InvalidFieldException(fieldName: String) extends ConfigException {

  override val reason: String = s"invalid field: $fieldName"

}

object ConfigException {

  def reasons(chain: NonEmptyChain[ConfigException]): Seq[String] =
    chain.foldLeft(Seq(""))((list, ex) => list :+ ex.reason).tail // get rid of leading empty string

}
