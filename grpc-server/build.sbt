val scala3Version = "3.1.2"

lazy val root = project
  .in(file("."))
  .settings(
        name := "spiderplan-server",
        version := "0.1.0",
        organization := "org.spiderplan",

        Compile / run / mainClass := Some("org.spiderplan.grpc.Main"),
        Compile / packageBin / mainClass := Some("org.spiderplan.grpc.Main"),

        isSnapshot := true,

        resolvers += Resolver.mavenCentral,
        resolvers += Resolver.mavenLocal,

        parallelExecution := false,

        libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9",
        libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test",
        libraryDependencies += "io.grpc" % "grpc-netty-shaded" % "1.51.0",
        libraryDependencies += "io.grpc" % "grpc-netty" % "1.51.0",

        libraryDependencies += "org.aiddl" % "aiddl-core-scala" % "1.1.1",
        libraryDependencies += "org.aiddl" % "aiddl-common-scala" % "0.3.0",
        libraryDependencies += "org.aiddl" % "aiddl-external-grpc-scala" % "0.2.0",
        libraryDependencies += "org.spiderplan" % "spiderplan" % "0.3.0-SNAPSHOT",

        scalaVersion := scala3Version
  )
