ThisBuild / organization := "itc.userDirectory"
ThisBuild / scalaVersion := "2.13.1"
ThisBuild / credentials += Credentials("Artifactory Realm", "10.64.49.169", "admin", "APBDczs2DbMKmxjmRTyGmVVgXTR")
ThisBuild / publishTo := {
  val artifactory = "http://10.64.49.169:8040/artifactory"
  if (isSnapshot.value) Some(("Artifactory Realm" at artifactory + "/sbt-dev-local").withAllowInsecureProtocol(true))
  else Some("Artifactory Realm" at artifactory + "/sbt-release-local")
}

lazy val akkaHttpVersion = "10.1.11"
lazy val akkaVersion     = "2.6.4"

lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging, ParadoxPlugin, ProtocPlugin)
  .settings(
    version := "0.0.1-SNAPSHOT",
    maintainer := "lart@pisem.net",
    name := "User Keys Catalog",
    normalizedName := "userKeysCatalog",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"    %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"    %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka"    %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka"    %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka"    %% "akka-slf4j"           % akkaVersion,
      "ch.qos.logback"       % "logback-classic"       % "1.2.3",
      "io.grpc"              % "grpc-netty"            % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime"      % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "com.typesafe.akka"    %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka"    %% "akka-testkit"         % akkaVersion % Test,
      "com.typesafe.akka"    %% "akka-stream-testkit"  % akkaVersion % Test,
      "org.scalatest"        %% "scalatest"            % "3.1.0" % Test
    ),
    PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value)
  )

