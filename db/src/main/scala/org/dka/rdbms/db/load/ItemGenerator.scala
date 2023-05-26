package org.dka.rdbms.db.load

import java.util.UUID
import java.io._
import scala.util.{Success, Try}

import ItemGenerator._

trait ItemGenerator {

  def count: Int

  def fileName: String

  def headerLine: String

  def insertLine(uuid: UUID): String

  def write(): Try[Seq[UUID]] =
    for {
      uuids  <- Success(Range.inclusive(1, count).toList.map(_ => UUID.randomUUID()))
      writer <- Try(new BufferedWriter(new FileWriter(new File(fileName))))
      _      <- Try(writer.write(headerLine))
      _      <- Try(writer.write(generateInsertBlock(uuids)))
    } yield {
      writer.close()
      uuids
    }

  private def generateInsertBlock(uuids: Seq[UUID]): String = replaceTrailingComma(uuids.map(insertLine))

}

object ItemGenerator {

  def replaceTrailingComma(lines: Seq[String]): String = {
    val reversed = lines.reverse
    val updated  = reversed.head.dropRight(1) + "\n;\n"
    (updated +: reversed.tail).reverse.mkString("\n")
  }

}
