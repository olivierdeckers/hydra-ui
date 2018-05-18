import com.lihaoyi.workbench.WorkbenchBasePlugin.autoImport.WorkbenchStartModes.Manual
import sbt.addCompilerPlugin

enablePlugins(WorkbenchPlugin)
workbenchStartMode := Manual

val http4sVersion = "0.18.9"

lazy val root = crossProject
  .in(file("."))
  .settings(
    scalaVersion := "2.12.4",
    organization := "be.olivierdeckers",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.6.5",
      "com.lihaoyi" %%% "autowire" % "0.2.6",
      "com.lihaoyi" %%% "utest" % "0.6.3" % Test,
      "org.typelevel" %%% "cats-core" % "1.1.0",
    ),
    scalacOptions += "-Ypartial-unification",
    testFrameworks += new TestFramework("utest.runner.Framework")
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
      "com.softwaremill.sttp" %% "akka-http-backend" % "1.1.12",
      "com.github.pureconfig" %% "pureconfig" % "0.9.1",
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "io.circe" %% "circe-generic" % "0.9.3",
      "org.slf4j" % "slf4j-simple" % "1.6.2",
      "com.lihaoyi" %% "ujson-circe" % "0.6.5",
    )
  )

val hydraUIJS = root.js
val hydraUIJVM = root.jvm.settings(
  (resources in Compile) += {
    (fastOptJS in(hydraUIJS, Compile)).value
    (artifactPath in(hydraUIJS, Compile, fastOptJS)).value
  },
  (resources in Compile) += {
    val js = (fastOptJS in (hydraUIJS, Compile)).value.data
    js.getParentFile / (js.getName + ".map")
  }
)