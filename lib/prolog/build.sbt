val scala3Version = "3.1.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "spiderplan_prolog",
    version := "0.1.0-SNAPSHOT",
    organization := "org.spiderplan",

    scalaVersion := scala3Version,
    crossPaths := false,

    resolvers += Resolver.mavenLocal,

    parallelExecution := false,

    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test",
    libraryDependencies += "org.aiddl" % "aiddl-common-scala" % "1.0.0-SNAPSHOT",
    libraryDependencies += "org.aiddl" % "aiddl-core-scala" % "1.0.0-SNAPSHOT",
    libraryDependencies += "org.aiddl" % "aiddl-util-scala" % "0.1.0-SNAPSHOT",
    libraryDependencies += "org.aiddl" % "aiddl-external-prolog" % "0.1.0-SNAPSHOT",
    libraryDependencies += "org.spiderplan" % "spiderplan" % "0.3.0-SNAPSHOT"
  )
