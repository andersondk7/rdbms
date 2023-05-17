package org.dka.rdbms.db.load

import java.time.{LocalDate, LocalDateTime}
import scala.util.Random

object Generator {
  def genString(length: Int, replace: String = " "): String = {
    val sb = new StringBuilder
    for (i <- 1 to length) {
      val index = util.Random.nextInt(allowedCharactersSize)
      sb.append(allowedCharacters(index))
    }
    sb.toString().replaceAll("_", replace)
  }

  def genPrice: BigDecimal = {
    val dollars = BigDecimal(random.nextInt(98) + 1)
    val cents = BigDecimal(random.nextInt(99)) / 100
    dollars + cents
  }

  def genWebSite(length: Int): String = {
    val prefix = "https://"
    val domain = ".com"
    val base = genString(length - prefix.length - domain.length, "/")
    prefix + base + domain
  }

  def genDate: LocalDate = {
    val now = LocalDate.now()
    now.minusWeeks(random.nextLong(2600)) // within the last 50 years
  }

  def genDateTime: LocalDateTime = {
    val now = LocalDateTime.now()
    now.minusWeeks(random.nextLong(2600)) // within the last 50 years
  }

  private val allowedCharacters: Seq[Char] = ('a' to 'z') ++ ('A' to 'Z') ++ Seq.fill(10)('_')
  private val allowedCharactersSize = allowedCharacters.size
  private val random = new Random(42)

}
