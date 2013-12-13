package example.sources

import com.twitter.scalding._
import com.twitter.scalding.Dsl._

case class Click(user: String, timestamp: Long, id: String)

object ClickSource {

  val MIN_FIELD_COUNT = 3

  def getField(input: Array[String], i: Int, default: String = "empty-field") = {
    if (input.isDefinedAt(i)) input(i) else default
  }

  def apply(p: RichPipe) = {
    p.mapTo('line -> ('click)) { line: String =>
      val split_input = line.split("\t")
      assert(split_input.size >= MIN_FIELD_COUNT)

      val user = getField(split_input, 0)
      val timestamp = getField(split_input, 1).toLong
      val id = getField(split_input, 2)

      (Click(user, timestamp, id))
    }
  }
}

