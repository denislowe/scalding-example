package example.config

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import scala.io.Source

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

/* example json:
{
    "customers": [
        {
            "name": "customer1",
            "reporting": {
                "tz": "America/New_York",
                "enabled": true
            }
        },
        {
            "name": "customer2",
            "reporting": {
                "tz": "GMT",
                "enabled": true
            }
        }
    ]
}
 */

case class Config(tz: String, enabled: Boolean, extract_brand: Option[Boolean])
case class User(name: String, reporting: Config)
case class Configs(users: List[User]) {
  def enabledUsers = users.filter(_.reporting.enabled)
}

object ReportConfig {
  def apply(file_path: String, force_hadoop_read: Boolean = false) = {

    val config_string = if ((file_path startsWith "s3:") || !(file_path startsWith "/") || force_hadoop_read)
      readHdfs(file_path)
    else
      Source.fromURL(getClass.getResource(file_path)).getLines.mkString

    implicit val formats = DefaultFormats
    parse(config_string).extract[Configs]
  }

  def readHdfs(file_path: String) = {
    val path = new Path(file_path)
    val fs = path.getFileSystem(new Configuration())
    val open_file = fs.open(path)
    val outputStr = Source.fromInputStream(open_file).mkString
    open_file.close()
    outputStr
  }
}