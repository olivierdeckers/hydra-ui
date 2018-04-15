addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.22")

// TODO enable this when new release is out
//addSbtPlugin("com.lihaoyi" % "workbench" % "0.4.0")
lazy val workbench = RootProject(uri("git://github.com/lihaoyi/workbench.git#75acf7d06cac7fe1798a585ee742005eac6d2ef9"))

lazy val root = (project in file(".")).dependsOn(workbench)