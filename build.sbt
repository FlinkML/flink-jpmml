resolvers in ThisBuild ++= Seq(
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaUnidocPlugin)
  .settings(
    name := "flink-jpmml",
    crossScalaVersions := Seq("2.10.6", "2.11.8"),
    publish := {},
    publishLocal := {},
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(`flink-jpmml-java`,
                                                                               `flink-jpmml-handson`,
                                                                               `flink-jpmml-assets`)
  )
  .aggregate(`flink-jpmml-handson`, `flink-jpmml-scala`, `flink-jpmml-java`, `flink-jpmml-assets`)

lazy val `flink-jpmml-assets` = project
  .enablePlugins(AutomateHeaderPlugin)
  .settings(LicenseSetting.settings: _*)
  .settings(Commons.settings: _*)
  .settings(PublishSettings.settings: _*)

lazy val `flink-jpmml-handson` = project
  .enablePlugins(AutomateHeaderPlugin)
  .settings(LicenseSetting.settings: _*)
  .settings(Commons.settings: _*)
  .settings(PublishSettings.settings: _*)
  .settings(libraryDependencies ++= Dependencies.Handson.addons)
  .dependsOn(`flink-jpmml-scala`)

lazy val `flink-jpmml-java` = project
  .enablePlugins(AutomateHeaderPlugin)
  .settings(LicenseSetting.settings: _*)
  .settings(Commons.settings: _*)
  .settings(PublishSettings.settings: _*)
  .settings(libraryDependencies ++= Dependencies.Java.addons)
  .dependsOn(`flink-jpmml-assets`)

lazy val `flink-jpmml-scala` = project
  .enablePlugins(AutomateHeaderPlugin)
  .settings(LicenseSetting.settings: _*)
  .settings(Commons.settings: _*)
  .settings(PublishSettings.settings: _*)
  .settings(libraryDependencies ++= Dependencies.Scala.addons)
  .dependsOn(`flink-jpmml-assets`)

onLoad in Global := (Command.process("scalafmt", _: State)) compose (onLoad in Global).value

// make run command include the provided dependencies
run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))

// exclude Scala library from assembly
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

// assign default options to JUnit test execution
testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

fork in test := true
