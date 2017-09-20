/*
 * Copyright (C) 2017  Radicalbit
 *
 * This file is part of flink-JPMML
 *
 * flink-JPMML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * flink-JPMML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with flink-JPMML.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.radicalbit.examples.util

import io.radicalbit.examples.model.Utils
import io.radicalbit.examples.sources.ControlSource
import org.apache.flink.api.java.utils.ParameterTool

object DynamicParams {

  def fromParameterTool(params: ParameterTool): DynamicParams = {

    val outputPath = params.getRequired("output")

    val pathsAndIds = retrievePathsAndIds(params.getRequired("models"))

    val policy = computeGenPolicy(params.get("gen-policy", "random"))

    val availableIdModels = computeAvailableIds(pathsAndIds)

    val intervalCheckpoint = params.get("intervalCheckpoint", 1000.toString).toLong

    val maxIntervalControlStream = params.get("maxIntervalControlStream", 5000L.toString).toLong

    DynamicParams(outputPath, policy, pathsAndIds, availableIdModels, intervalCheckpoint, maxIntervalControlStream)
  }

  private def retrievePathsAndIds(paths: String) = {
    val rawModelsPaths = paths.split(",")
    Utils.retrieveMappingIdPath(rawModelsPaths)
  }

  private def computeGenPolicy(rawPolicy: String) =
    rawPolicy match {
      case "random" => ControlSource.Random
      case "loop" => ControlSource.Loop
      case "finite" => ControlSource.Finite
      case _ => throw new IllegalArgumentException(s"$rawPolicy is not recognized generation policy.")
    }

  private def computeAvailableIds(pathsAndIds: Map[String, String]) =
    Utils.retrieveAvailableId(pathsAndIds)

}

case class DynamicParams(outputPath: String,
                         genPolicy: ControlSource.Mode,
                         pathAndIds: Map[String, String],
                         availableIds: Seq[String],
                         ckpInterval: Long,
                         ctrlGenInterval: Long)
