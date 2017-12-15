import com.typesafe.config.ConfigFactory
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Code example for:
  *
  * http://docs.aws.amazon.com/redshift/latest/gsg/rs-gsg-create-sample-db.html
  */
object LoadDataExample extends App {

  val db = Database.forConfig("slick.redshift")
  val s3Settings = S3Settings(ConfigFactory.load())

  def dropTables(): Future[Unit] = db.run(Tables.dropAll())

  def createTables(): Future[Unit] = db.run(Tables.createAll())

  def vacuumAndAnalyze(): Future[Int] = {
    // See: http://docs.aws.amazon.com/redshift/latest/dg/tutorial-loading-data-vacuum.html
    db.run(sqlu"vacuum; analyze;")
  }

  def loadSampleData(): Future[Unit] = {
    def buildCopyQuery(tableName: String, dataSource: String, credentials: String, delimiter: String, region: String): DBIO[Int] = {
      sqlu"""copy #$tableName from '#$dataSource'
      credentials '#$credentials'
      delimiter '#$delimiter' region '#$region';"""
    }

    val region = s3Settings.region
    val credentials = s"aws_iam_role=${s3Settings.roleArn}"

    db.run(DBIO.seq(
      buildCopyQuery("users", "s3://awssampledbuswest2/tickit/allusers_pipe.txt", credentials, "|", region),
      buildCopyQuery("venue", "s3://awssampledbuswest2/tickit/venue_pipe.txt", credentials, "|", region),
      buildCopyQuery("category", "s3://awssampledbuswest2/tickit/category_pipe.txt", credentials, "|", region),
      buildCopyQuery("date", "s3://awssampledbuswest2/tickit/date2008_pipe.txt", credentials, "|", region),
      buildCopyQuery("event", "s3://awssampledbuswest2/tickit/allevents_pipe.txt", credentials, "|", region),
      buildCopyQuery("listing", "s3://awssampledbuswest2/tickit/listings_pipe.txt", credentials, "|", region)
      // buildCopyQuery("sales", "s3://awssampledbuswest2/tickit/sales_tab.txt", credentials, "\\t", region)
    ))
  }

  val result: Future[Unit] =
    for {
      _ <- dropTables()
      _ <- vacuumAndAnalyze()
      _ <- createTables()
      result <- loadSampleData()
    } yield result

  result onComplete {
    case _ => db.close
  }

  Await.result(result, Duration.Inf)

}
