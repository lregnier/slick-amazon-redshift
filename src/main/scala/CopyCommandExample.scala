import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Code example from:
  *
  * http://docs.aws.amazon.com/redshift/latest/gsg/rs-gsg-create-sample-db.html
  */
object CopyCommandExample extends App {
  case class AwsContext(credentials: String, region: String)

  val db = Database.forConfig("redshift")
  implicit val awsContext = AwsContext("aws_iam_role=arn:aws:iam::582312427336:role/myRedshiftRole", "us-west-2")

  try {

    def buildCopyQuery(tableName: String, dataSource: String, delimiter: String = "|")(implicit awsContext: AwsContext): DBIO[Int] = {
      sqlu"""copy #$tableName from '#$dataSource'
        credentials '#${awsContext.credentials}'
        delimiter '#$delimiter' region '#${awsContext.region}';"""
    }

    val dropTablesResult = db.run(Tables.dropAll())

    Await.result(dropTablesResult, Duration.Inf)

    val createTablesResult = db.run(Tables.createAll())

    Await.result(createTablesResult, Duration.Inf)

    val loadSampleDataResult =
      db.run(DBIO.seq(
        buildCopyQuery("users", "s3://awssampledbuswest2/tickit/allusers_pipe.txt"),
        buildCopyQuery("venue", "s3://awssampledbuswest2/tickit/venue_pipe.txt"),
        buildCopyQuery("category", "s3://awssampledbuswest2/tickit/category_pipe.txt"),
        buildCopyQuery("date", "s3://awssampledbuswest2/tickit/date2008_pipe.txt"),
        buildCopyQuery("event", "s3://awssampledbuswest2/tickit/allevents_pipe.txt"),
        buildCopyQuery("listing", "s3://awssampledbuswest2/tickit/listings_pipe.txt"),
        buildCopyQuery("sales", "s3://awssampledbuswest2/tickit/sales_tab.txt")
      ))

    Await.result(loadSampleDataResult, Duration.Inf)

  } finally db.close

}
