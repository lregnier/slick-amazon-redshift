name := "slick-amazon-redshift"

version := "0.1"

scalaVersion := "2.12.4"

resolvers ++= Seq(
  "Redsfhit" at "http://redshift-maven-repository.s3-website-us-east-1.amazonaws.com/release"
)

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "3.2.0",
  "org.slf4j" % "slf4j-nop" % "1.7.10",
  "com.amazon.redshift" % "redshift-jdbc42" % "1.2.10.1009"
)