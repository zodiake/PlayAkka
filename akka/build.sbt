name := "akkaFileDeploy"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaVersion = "2.4.10"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5"
  )
}
