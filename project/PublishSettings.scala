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

import com.typesafe.sbt.SbtPgp.autoImportImpl.PgpKeys
import sbt.Keys.{publishMavenStyle, _}
import sbt.{Def, url, _}
import sbtrelease.ReleasePlugin.autoImport.{releaseCrossBuild, releasePublishArtifactsAction}
import xerial.sbt.Sonatype._

object PublishSettings {

  lazy val settings: Seq[Def.Setting[_]] = sonatypeSettings ++ Seq(
    publishTo := Some(
      if (isSnapshot.value)
        Opts.resolver.sonatypeSnapshots
      else
        Opts.resolver.sonatypeStaging
    ),
    publishMavenStyle := true,
    licenses := Seq("AGPL-3.0" -> url("https://opensource.org/licenses/AGPL-3.0")),
    homepage := Some(url("https://github.com/FlinkML/flink-jpmml")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/FlinkML/flink-jpmml"),
        "scm:git:git@github.com:FlinkML/flink-jpmml.git"
      )
    ),
    developers := List(
      Developer(id = "spi-x-i",
                name = "Andrea Spina",
                email = "andrea.spina@radicalbit.io",
                url = url("https://github.com/spi-x-i")),
      Developer(id = "francescofrontera",
                name = "Francesco Frontera",
                email = "francesco.frontera@radicalbit.io",
                url = url("https://github.com/francescofrontera")),
      Developer(id = "riccardo14",
                name = "Riccardo Diomedi",
                email = "riccardo.diomedi@radicalbit.io",
                url = url("https://github.com/riccardo14")),
      Developer(id = "maocorte",
                name = "Mauro Cortellazzi",
                email = "mauro.cortellazzi@radicalbit.io",
                url = url("https://github.com/maocorte"))
    ),
    autoAPIMappings := true,
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value
  )
}
