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

package io.radicalbit.examples

import io.radicalbit.examples.models.Iris
import io.radicalbit.examples.sources.{ControlSource, IrisSource}
import io.radicalbit.examples.util.DynamicParams
import io.radicalbit.flink.pmml.scala._
import io.radicalbit.flink.pmml.scala.api.PmmlModel
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala._

/**
  * Toy Job about Stateful Dynamic Model Serving:
  * The job owns two input streams:
  * - event stream: The main input events stream
  * - control stream: Control messages about model repository server current state
  *
  */
object DynamicEvaluateKmeans {

  def main(args: Array[String]): Unit = {

    val parameterTool = ParameterTool.fromArgs(args)

    val parameters = DynamicParams.fromParameterTool(parameterTool)

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(parameters.ckpInterval, CheckpointingMode.EXACTLY_ONCE)

    val eventStream = IrisSource.irisSource(env, Option(parameters.availableIds))
    val controlStream =
      ControlSource.generateStream(env, parameters.genPolicy, parameters.pathAndIds, parameters.ctrlGenInterval)

    val predictions = eventStream
      .withSupportStream(controlStream)
      .evaluate { (event: Iris, model: PmmlModel) =>
        val vectorized = event.toVector
        val prediction = model.predict(vectorized, Some(0.0))
        (event, prediction.value)
      }

    predictions.writeAsText(parameters.outputPath)

    env.execute("Dynamic Clustering Example")
  }

}
