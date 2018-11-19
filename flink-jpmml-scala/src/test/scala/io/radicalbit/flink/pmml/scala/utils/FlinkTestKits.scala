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

package io.radicalbit.flink.pmml.scala.utils

import io.radicalbit.flink.pmml.scala.sources.TemporizedSourceFunction
import io.radicalbit.flink.streaming.spec.core.FlinkTestKitCompanion
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.test.util.AbstractTestBase

import scala.collection.mutable
import scala.reflect.ClassTag

trait FlinkSourcedPipelineTestKit[IN1, IN2, OUT] extends AbstractTestBase {

  def executePipeline[IN1: TypeInformation: ClassTag, IN2: TypeInformation: ClassTag](
      in1: Seq[(Long, IN1)],
      in2: Seq[(Long, IN2)])(pipeline: (DataStream[IN1], DataStream[IN2]) => DataStream[OUT])(
      implicit companion: FlinkTestKitCompanion[OUT]) = {

    companion.testResults = mutable.MutableList[OUT]()

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val events = in1
      .union(in2)
      .sortBy(_._1)
      .collect {
        case (_, left: IN1) => (Some(left), None)
        case (_, right: IN2) => (None, Some(right))
      }

    val stream = env.addSource(new TemporizedSourceFunction[IN1, IN2](events))

    val stream1: DataStream[IN1] = stream.filter(either => either.isLeft).map(either => either.left.get)
    val stream2: DataStream[IN2] = stream.filter(either => either.isRight).map(either => either.right.get)

    pipeline(stream1, stream2)
      .addSink(new SinkFunction[OUT] {
        override def invoke(in: OUT) = {
          companion.testResults += in
        }
      })

    env.execute(this.getClass.getSimpleName)

    companion.testResults
  }

}
