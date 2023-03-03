val scala3Version = "3.1.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "spiderplan",
    version := "0.3.0-SNAPSHOT",
    organization := "org.spiderplan",

    isSnapshot := true,
    scalaVersion := scala3Version,
    crossPaths := false,

    resolvers += Resolver.mavenLocal,

    parallelExecution := false,

    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test",
    libraryDependencies += "org.aiddl" % "aiddl-common-scala" % "ß.1.0",
    libraryDependencies += "org.aiddl" % "aiddl-core-scala" % "1.0.0"
  )
