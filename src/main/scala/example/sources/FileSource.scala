package example.sources

import com.twitter.scalding.DateOps
import com.twitter.scalding.DateRange
import com.twitter.scalding.TextLineScheme
import com.twitter.scalding.TimePathedSource

/**
 * For querying sources in directories with the following pattern:
 * filePath/logType/day=yyyy-MM-dd/
 * 
 * e.g.
 * s3://logfiles/impression/day=2013-12-01/file.gz
 * 
 * filePath = /file-path
 * logType = impression
 * 
 */
object FileSource {
  def apply(prefix: String, dataset: String)(implicit dateRange: DateRange) = new FileSource(prefix, dataset)
}

class FileSource(filePath: String, logType: String)(override implicit val dateRange: DateRange)
  extends TimePathedSource(filePath + "/" + logType + "/day=" + "%1$tY-%1$tm-%1$td" + "/*", dateRange, DateOps.UTC) with TextLineScheme