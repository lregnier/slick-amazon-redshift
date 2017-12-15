import com.typesafe.config.ConfigFactory
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Code example from:
  *
  * http://docs.aws.amazon.com/redshift/latest/gsg/rs-gsg-create-sample-db.html
  */
object CopyCommandExample extends App {

  implicit val awsContext = AwsContext.fromConfig(ConfigFactory.load())
  val db = Database.forConfig("slick.redshift")

  def dropTables(): Future[Unit] = db.run(Tables.dropAll())

  def createTables(): Future[Unit] = db.run(Tables.createAll())

  def loadSampleData(): Future[Unit] = {
    def buildCopyQuery(tableName: String, dataSource: String, delimiter: String = "|")(implicit awsContext: AwsContext): DBIO[Int] = {
      sqlu"""copy #$tableName from '#$dataSource'
      credentials 'aws_iam_role=#${awsContext.roleArn}'
      delimiter '#$delimiter' region '#${awsContext.region}';"""
    }

    db.run(DBIO.seq(
      buildCopyQuery("users", "s3://awssampledbuswest2/tickit/allusers_pipe.txt"),
      buildCopyQuery("venue", "s3://awssampledbuswest2/tickit/venue_pipe.txt"),
      buildCopyQuery("category", "s3://awssampledbuswest2/tickit/category_pipe.txt"),
      buildCopyQuery("date", "s3://awssampledbuswest2/tickit/date2008_pipe.txt"),
      buildCopyQuery("event", "s3://awssampledbuswest2/tickit/allevents_pipe.txt"),
      buildCopyQuery("listing", "s3://awssampledbuswest2/tickit/listings_pipe.txt"),
      buildCopyQuery("sales", "s3://awssampledbuswest2/tickit/sales_tab.txt")
    ))
  }

  val result: Future[Unit] =
    for {
      _ <- dropTables()
      _ <- createTables()
      result <- loadSampleData()
    } yield result

  result onComplete {
    case _ => db.close
  }

  Await.result(result, Duration.Inf)

}
