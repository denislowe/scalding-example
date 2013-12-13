package example.util

import org.apache.hadoop
import com.twitter.scalding.Tool

/**
 * Scalding job launcher entry point
 */
object JobRunner {
  def main(args: Array[String]) {
    hadoop.util.ToolRunner.run(new hadoop.conf.Configuration, new Tool, args);
  }
}
