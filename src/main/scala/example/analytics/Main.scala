package example.analytics

import com.twitter.scalding._
import com.twitter.scalding.DateOps._
import java.util.TimeZone
import cascading.pipe.joiner._
import example.config.ReportConfig
import example.sources._

/*
sbt/sbt \
"run example.analytics.Main \
--impressions data/input/impression.log \
--clicks data/input/click.log \
--output data/output \
--date 2012-10-01 2012-10-02 \
--local"
*/
class Main(args: Args) extends Job(args) with UtcDateRangeJob {

  val output = args("output")
  val analyticsConfig = ReportConfig(args.getOrElse("config", "src/main/resources/report_config.json"))

  // requires --date command line arg to be set
  def getDateRange(timeZone: TimeZone = DateOps.UTC) = {
    DateRange(RichDate(dateRange.start.toString(DateOps.DATE_WITH_DASH))(timeZone),
      RichDate(dateRange.end.toString(DateOps.DATE_WITH_DASH))(timeZone) + Days(1) - Millisecs(1))
  }

  val enabledUsers = analyticsConfig.enabledUsers.map { x =>
    val timeZone = TimeZone.getTimeZone(x.reporting.tz)
    (x.name -> getDateRange(timeZone))
  }.toMap

  /*
   * FileSource cannot be used in local mode - as only single files can be processed
   */
  def getSource(logType: String) = {
    if (args.boolean("local")) TextLine(args(logType))
    else FileSource(args("input"), logType)(getDateRange())
  }

  // process impressions
  val impressions = ImpressionSource(getSource("impressions"))
    // filter on active customers(config file) within a specific date range(command line args)
    .filter('impression) { i: Impression => enabledUsers.contains(i.user) && enabledUsers(i.user).contains(i.timestamp) }
    .filter('impression) { i: Impression => i.is_test == 0 }
    // the id will be used when joining with the clicks
    .map('impression -> ('id)) { i: Impression => (i.id) }

  // process clicks and join with impressions
  val clicks = ClickSource(getSource("clicks"))
    .map('click -> 'id) { c: Click => c.id }
    .joinWithLarger('id -> 'id, impressions)
    .insert('type, "click")
    .project('impression, 'id, 'type)

  // union impressions with clicks
  val cleanData = (impressions.insert('type, "impression") ++ clicks)
  
  cleanData.write(Tsv("data/tmp/clean.txt"))
  
  /**********************************************
   * Generate some analytics using the cleanData pipe
   **********************************************/
  
  /*
   * Count by user 
   */
  cleanData
  	.map('impression -> 'user) {i: Impression => i.user}
    .groupBy('user) {_.size}
    .write(Tsv(output + "/user-count.txt"))
    .groupAll {_.sum('size)}
    .write(Tsv(output + "/total-count.txt"))
    
  /*
   * Generate CTR and pivot output by impression, click counts
   */
  cleanData
    .map('impression -> ('user, 'city)) { i: Impression => (i.user, i.city) }
    .groupBy('user, 'city, 'type) { _.size("ctr") }
    .groupBy('user, 'city) { _.pivot(('type, 'ctr) -> ('impression, 'click), 0) }
    .write(Tsv(output + "/ctr.txt"))
}