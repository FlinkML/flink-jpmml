import sbt._
import sbt.Keys._

object Dependencies {

  object Scala {

    lazy val addons = Seq(
      flink.scalaCore % Provided,
      flink.streaming % Provided,
      flink.clients % Provided,
      flink.ml,
      jpmml.evaluator,
      // Test utils
      asm.assembly % Test,
      flink.utils % Test,
      hadoop.common % "test" classifier "tests",
      hadoop.hdfs % "test" classifier "tests",
      hadoop.mincluster % "test",
      scalatest.scalatest % Test
    )
  }

  object Java {

    lazy val addons = Seq(
      flink.core % Provided,
      flink.streaming % Provided,
      flink.clients % Provided,
      flink.ml,
      jpmml.evaluator,
      // Test utils
      asm.assembly % Test,
      flink.utils % Test,
      hadoop.common % "test" classifier "tests",
      hadoop.hdfs % "test" classifier "tests",
      hadoop.mincluster % "test",
      junitinterface.interface % Test
    )
  }

  object Handson {

    lazy val addons = Seq(
      flink.scalaCore % Provided,
      flink.streaming % Provided
    )
  }

  private object flink {
    lazy val namespace = "org.apache.flink"
    lazy val version = "1.2.0"
    lazy val core = namespace % "flink-core" % version
    lazy val scalaCore = namespace %% "flink-scala" % version
    lazy val streaming = namespace %% "flink-streaming-scala" % version
    lazy val ml = namespace %% "flink-ml" % version
    lazy val clients = namespace %% "flink-clients" % version
    lazy val utils = namespace %% "flink-test-utils" % version
  }

  private object jpmml {
    lazy val namespace = "org.jpmml"
    lazy val version = "1.3.5"
    lazy val evaluator = namespace % "pmml-evaluator" % version
  }

  object logging {
    lazy val namespace = "com.typesafe.scala-logging"
    lazy val version210 = "2.1.2"
    lazy val version211 = "3.5.0"
    lazy val lazyLogging210 = namespace %% "scala-logging" % version210
    lazy val lazyLogging211 = namespace %% "scala-logging" % version211
  }

  /*** Test utils ***/
  private object asm {
    lazy val namespace = "asm"
    lazy val version = "3.3.1"
    lazy val assembly = namespace % "asm" % version
  }

  private object hadoop {
    lazy val namespace = "org.apache.hadoop"
    lazy val version = "2.3.0"
    lazy val hdfs = namespace % "hadoop-hdfs" % version
    lazy val common = namespace % "hadoop-common" % version
    lazy val mincluster = namespace % "hadoop-minicluster" % version
  }

  private object junitinterface {
    lazy val namespace = "com.novocode"
    lazy val version = "0.11"
    lazy val interface = namespace % "junit-interface" % version
  }

  private object scalatest {
    lazy val namespace = "org.scalatest"
    lazy val version = "3.0.1"
    lazy val scalatest = namespace %% "scalatest" % version
  }

}
