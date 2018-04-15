import sbt.addCompilerPlugin

enablePlugins(WorkbenchPlugin)

lazy val root = crossProject
  .in(file("."))
  .settings(
    scalaVersion := "2.12.5",
    organization := "be.olivierdeckers",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.6.5",
      "com.lihaoyi" %%% "autowire" % "0.2.6",
      "com.lihaoyi" %%% "utest" % "0.6.3",
    )
  )
  .jsSettings(
    name := "client",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.5",
      "com.thoughtworks.binding" %%% "dom" % "11.0.1",
      "com.thoughtworks.binding" %%% "route" % "11.0.1",
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )
  .jvmSettings(
    name := "server",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "1.1.0",
      "com.typesafe.akka" %% "akka-http" % "10.1.1",
      "com.typesafe.akka" %% "akka-stream" % "2.5.11",
      "org.webjars.npm" % "materialize-css" % "1.0.0-beta",
      "com.softwaremill.sttp" %% "akka-http-backend" % "1.1.12"
    )
  )

val hydraUIJS = root.js
val hydraUIJVM = root.jvm.settings(
  (resources in Compile) += {
    (fastOptJS in(hydraUIJS, Compile)).value
    (artifactPath in(hydraUIJS, Compile, fastOptJS)).value
  }
)