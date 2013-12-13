package example.sources

import com.twitter.scalding._
import com.twitter.scalding.Dsl._

case class Impression(user: String, timestamp: Long, id: String, category: String, os: String, city: String, is_test: Int )

object ImpressionSource {

  val MIN_FIELD_COUNT = 7

  def getField(input: Array[String], i: Int, default: String = "empty-field") = {
    if (input.isDefinedAt(i)) input(i) else default
  }

  def apply(p: RichPipe) = {
    p.mapTo('line -> ('impression)) { line: String =>
      val split_input = line.split("\t")
      assert(split_input.size >= MIN_FIELD_COUNT)
      val user = getField(split_input, 0)
      val timestamp = getField(split_input, 1).toLong
      val id = getField(split_input, 2)
      val category = getField(split_input, 3)
      val os = getField(split_input, 4)
      val city = getField(split_input, 5)
      val is_test = getField(split_input, 6).toInt
      (Impression(user, timestamp, id, category, os, city, is_test))
    }
  }
}

