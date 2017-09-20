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

import io.radicalbit.examples.model.Utils
import io.radicalbit.examples.models.Iris
import io.radicalbit.flink.pmml.scala.api.PmmlModel
import io.radicalbit.flink.pmml.scala.models.control.AddMessage
import io.radicalbit.flink.pmml.scala.models.core.ModelId
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.streaming.api.scala._

import scala.util.Random

object CheckpointEvaluate {

  private final lazy val idSet = Set(
    "4897c9f4-5226-43c7-8f2d-f9fd388cf2bc",
    "5f919c52-2ef8-4ff2-94b2-2e64bb85005e"
  )

  def main(args: Array[String]): Unit = {
    val parameterTool = ParameterTool.fromArgs(args)

    val outputPath = parameterTool.getRequired("output")

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE)

    val eventStream = env.addSource((sc: SourceContext[Iris]) => {
      val NumberOfParameters = 4
      lazy val RandomGenerator = scala.util.Random
      val RandomMin = 0.2
      val RandomMax = 6.0

      @inline def truncateDouble(n: Double) = (math floor n * 10) / 10

      while (true) {
        def randomVal = RandomMin + (RandomMax - RandomMin) * RandomGenerator.nextDouble()
        val dataForIris = Seq.fill(NumberOfParameters)(truncateDouble(randomVal))
        val iris =
          Iris(idSet.toVector(Random.nextInt(idSet.size)) + ModelId.separatorSymbol + "1",
               dataForIris.head,
               dataForIris(1),
               dataForIris(2),
               dataForIris.last,
               Utils.now())
        sc.collect(iris)
        Thread.sleep(1000)
      }
    })

    val controlStream = env
      .socketTextStream("localhost", 9999)
      .map(path => AddMessage(idSet.toVector(Random.nextInt(idSet.size)), 1L, path, Utils.now()))

    val predictions = eventStream
      .withSupportStream(controlStream)
      .evaluate { (event: Iris, model: PmmlModel) =>
        val vectorized = event.toVector
        val prediction = model.predict(vectorized, Some(0.0))
        (event, prediction.value)
      }

    predictions
      .writeAsText(outputPath, WriteMode.OVERWRITE)
      .setParallelism(2)

    env.execute("Checkpoint Evaluate Example")
  }
}
