lazy val akkaHttpVersion = "10.1.9"
lazy val akkaVersion = "2.5.24"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "itc.usrDir",
      scalaVersion := "2.13.0",
      version := "0.0.1-SNAPSHOT",
    )),
    name := "userCatalog",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",

      "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",

      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test
    )
  )

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

inConfig(Test)(sbtprotoc.ProtocPlugin.protobufConfigSettings)
