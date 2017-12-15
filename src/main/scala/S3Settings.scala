import com.typesafe.config.Config

object S3Settings {
  def apply(config: Config): S3Settings = new S3Settings(config)
}

class S3Settings(config: Config) {
  private val s3Config = config.getConfig("aws.s3")
  val region = s3Config.getString("region")
  val roleArn = s3Config.getString("role-arn")
}