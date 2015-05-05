lazy val commonSettings = Seq(
  organization := "com.nummulus.amqp.driver",
  version := "0.2.0-SNAPSHOT",

  scalaVersion := "2.11.6",

  scalacOptions ++= Seq("-deprecation", "-optimise", "-explaintypes")
)

/**
 * Projects
 */
lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "amqp-parent"
  )
  .aggregate(driver, blackbox, integrationTest)

lazy val driver = (project in file("amqp-driver"))
  .settings(commonSettings: _*)
  .settings(
    name := "amqp-driver",

    libraryDependencies ++= driverDependencies
  )

lazy val blackbox = (project in file("amqp-driver-blackbox"))
  .settings(commonSettings: _*)
  .settings(
    name := "amqp-driver-blackbox",

    libraryDependencies ++= blackboxDependencies
  )
  .dependsOn(driver)

lazy val integrationTest = (project in file("amqp-driver-test"))
  .settings(commonSettings: _*)
  .settings(
    name := "amqp-driver-test",

    libraryDependencies ++= testDependencies
  )
  .dependsOn(driver)

/**
 * Dependencies
 */
lazy val akkaVersion = "2.3.10"
lazy val scalatestVersion = "2.2.4"

lazy val driverDependencies = Seq(
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "com.rabbitmq" % "amqp-client" % "3.1.3",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion
) ++ testDependencies

lazy val blackboxDependencies = Seq(
  "org.scalatest" %% "scalatest" % scalatestVersion
) ++ testDependencies

lazy val testDependencies = Seq(
  "junit" % "junit" % "4.11" % Test,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "org.mockito" % "mockito-all" % "1.9.5" % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
)
