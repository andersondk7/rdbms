package org.dka.rdbms.db.load

import cats.Show

import scala.util.{Failure, Success}
import scala.annotation.tailrec
import scala.sys.exit

final case class Params(
  country: Int,
  location: Int,
  publisher: Int,
  author: Int,
  book: Int)

object Params {

  private val country   = "country"

  private val location  = "location"

  private val publisher = "publisher"

  private val author    = "author"

  private val book      = "book"

  private val usage =
    s"Usage: generator: [--$country c], [--$location l], [--$publisher p], [--$author a], [--$book t]"

  def apply(map: Map[String, Int]): Params =
    new Params(
      map.getOrElse(country, 5),
      map.getOrElse(location, 10),
      map.getOrElse(publisher, 10),
      map.getOrElse(author, 100),
      map.getOrElse(book, 200)
    )

  def apply(args: Array[String]): Either[String, Params] = Right(Params(5, 5, 5, 5, 5))
//  def apply(args: Array[String]): Either[String, Params] = if (args.isEmpty) Left(usage)
//  else {
//    Right(Params(nextArg(Map(), args.toList)))
//  }

  @tailrec
  private def nextArg(map: Map[String, Int], list: List[String]): Map[String, Int] =
    list match {
      case Nil => map
      case param :: value :: tail =>
        nextArg(map ++ Map(param.drop(2) -> value.toInt), tail)
      case unknown :: _ =>
        println(s"\nunknown option $unknown\n")
        exit(1)
    }

  implicit val showParams: Show[Params] = Show.show(params =>
    s"counts: " +
      s"country: ${params.country}, " +
      s"location: ${params.location}, " +
      s"publisher: ${params.publisher} " +
      s"author: ${params.author} " +
      s"book: ${params.book}")

}

object GeneratorApp extends App {

  Params.apply(args) match {
    case Left(errorMessage) => println(errorMessage)
    case Right(params) =>
      val countryGenerator = new CountryGenerator(params.country)
      val result = for {
        countryUUIDs <- countryGenerator.write()
//        locationGenerator = new LocationGenerator(params.location, countryUUIDs)
//        locationUUIDs <- locationGenerator.write()
//        publisherGenerator = new PublisherGenerator(params.publisher, locationUUIDs)
//        publisherUUIDs <- publisherGenerator.write()
//        authorGenerator = new AuthorGenerator(params.author, locationUUIDs)
//        authorUUIDs <- authorGenerator.write()
//        bookGenerator = new BookGenerator(params.book, publisherUUIDs)
//        bookUUIDs <- bookGenerator.write()
//        authorsBooksGenerator = new AuthorsBooksGenerator(authorUUIDs, bookUUIDs)
//        _ <- authorsBooksGenerator.write()
      } yield println(s"generated ${countryUUIDs.size} countries")
//        println(s"generated ${locationUUIDs.size} locations")
//        println(s"generated ${publisherUUIDs.size} publishers")
//        println(s"generated ${authorUUIDs.size} authors")
//        println(s"generated ${bookUUIDs.size} books")
      result match {
        case Failure(error) =>
          println(s"did not write lines because: $error")
          exit(1)
        case Success(_) => println("success!")
      }
  }

}
