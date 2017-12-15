import com.typesafe.config.Config

case class AwsContext(region: String, roleArn: String)

object AwsContext {
  def fromConfig(config: Config): AwsContext = {
    val awsConfig = config.getConfig("aws")
    val region = awsConfig.getString("region")
    val roleArn = awsConfig.getString("role-arn")

    AwsContext(region, roleArn)
  }
}
