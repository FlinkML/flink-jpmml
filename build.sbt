resolvers in ThisBuild ++= Seq(
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)

name := "flink-jpmml"

lazy val commonSettings = Seq(
  organization := "io.radicalbit",
  scalaVersion in ThisBuild := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  crossPaths := false
)

lazy val root = project
  .in(file("."))
  .aggregate(`flink-jpmml-handson`, `flink-jpmml-scala`, `flink-jpmml-java`, `flink-jpmml-assets`)

lazy val `flink-jpmml-assets` = project
  .settings(commonSettings: _*)

lazy val `flink-jpmml-handson` = project
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.Handson.addons)
  .dependsOn(`flink-jpmml-scala`)

lazy val `flink-jpmml-java` = project
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.Java.addons)
  .dependsOn(`flink-jpmml-assets`)

lazy val `flink-jpmml-scala` = project
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.Scala.addons)
  .settings(libraryDependencies ++= Seq(CrossVersion partialVersion scalaVersion.value match {
    case Some((2, major)) if major >= 11 => Dependencies.logging.lazyLogging211
    case _ => Dependencies.logging.lazyLogging210
  }))
  .dependsOn(`flink-jpmml-assets`)

onLoad in Global := (Command.process("scalafmt", _: State)) compose (onLoad in Global).value

// make run command include the provided dependencies
run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))

// exclude Scala library from assembly
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

// assign default options to JUnit test execution
testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

fork in test := true
