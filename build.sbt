
lazy val root = (project in file("."))
  .settings(
    name := "PlayWebWithAkka"
  )
  .aggregate(web, akka)
lazy val web = (project in file("web"))
  .enablePlugins(PlayScala)
  .settings(
    name := "web",
    organization := "com.nielsen",
    version := "1.0",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(jdbc,
      cache,
      ws,
      specs2 % Test,
      "com.typesafe.play" %% "anorm" % "2.5.0",
      "com.typesafe.akka" %% "akka-remote" % "2.4.10",
      evolutions),
    resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
  ).dependsOn(akka)
lazy val akka = (project in file("akka"))
  .settings(
    name := "akka",
    organization := "com.nielsen",
    version := "1.0",
    scalaVersion := "2.11.7"
  )

