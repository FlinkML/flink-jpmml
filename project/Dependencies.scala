import sbt._
import sbt.Keys._
/*
 *
 * Copyright (c) 2017 Radicalbit
 *
 * This file is part of flink-JPMML
 *
 * flink-JPMML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * flink-JPMML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with flink-JPMML.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

object Dependencies {

  object Scala {

    lazy val libraries = Seq(
      flink.scalaCore % Provided,
      flink.streaming % Provided,
      flink.clients % Provided,
      flink.ml,
      jpmml.evaluator,
      logging.slf4j,
      // Test utils
      asm.assembly % Test,
      flink.utils % Test,
      hadoop.common % "test" classifier "tests",
      hadoop.hdfs % "test" classifier "tests",
      hadoop.mincluster % "test",
      scalatest.scalatest % Test,
      `flink-streaming-spec`.core % Test
    )
  }

  object Examples {

    lazy val libraries = Seq(
      flink.scalaCore % Provided,
      flink.streaming % Provided
    )

  }

  private object flink {
    lazy val namespace = "org.apache.flink"
    lazy val version = "1.3.2"
    lazy val core = namespace % "flink-core" % version
    lazy val scalaCore = namespace %% "flink-scala" % version
    lazy val streaming = namespace %% "flink-streaming-scala" % version
    lazy val ml = namespace %% "flink-ml" % version
    lazy val clients = namespace %% "flink-clients" % version
    lazy val utils = namespace %% "flink-test-utils" % version
  }

  private object jpmml {
    lazy val namespace = "org.jpmml"
    lazy val version = "1.3.9"
    lazy val evaluator = namespace % "pmml-evaluator" % version
  }

  object logging {
    lazy val namespace = "org.slf4j"
    lazy val version = "1.7.7"
    lazy val slf4j = namespace % "slf4j-api" % version
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

  private object `flink-streaming-spec` {
    lazy val namespace = "io.radicalbit"
    lazy val version = "0.0.1"
    lazy val core = namespace %% "flink-streaming-spec" % version
  }

}
