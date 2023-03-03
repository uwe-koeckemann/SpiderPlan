val scala3Version = "3.1.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "spiderplan_coorination_oru",
    version := "0.1.0-SNAPSHOT",
    organization := "org.spiderplan",

    scalaVersion := scala3Version,
    crossPaths := false,

    resolvers += Resolver.mavenLocal,
    resolvers += Resolver.jcenterRepo,
    resolvers += "jitpack Repo" at "https://jitpack.io/",
    resolvers += "rosjava Repo" at "https://github.com/rosjava/rosjava_mvn_repo/raw/master/",


    parallelExecution := false,

    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test",
    libraryDependencies += "org.aiddl" % "aiddl-common-scala" % "0.1.0",
    libraryDependencies += "org.aiddl" % "aiddl-core-scala" % "1.0.0",
    libraryDependencies += "org.aiddl" % "aiddl-external-coordination_oru" % "0.1.0-SNAPSHOT",
    libraryDependencies += "org.spiderplan" % "spiderplan" % "0.3.0-SNAPSHOT"
  )
