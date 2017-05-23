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

package io.radicalbit.flink.pmml.scala.utils

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.util.StreamingMultipleProgramsTestBase
import org.scalatest._

import scala.collection.mutable

trait FlinkTestKitCompanion[T] {
  var testResults: mutable.MutableList[T] = null
}

trait FlinkPipelineTestKit[IN, OUT] extends StreamingMultipleProgramsTestBase with WordSpecLike with Matchers {

  def run[IN: TypeInformation](in: Seq[IN], out: Seq[OUT], companion: FlinkTestKitCompanion[OUT])(
      pipeline: DataStream[IN] => DataStream[OUT]) = {

    companion.testResults = mutable.MutableList[OUT]()

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    val stream = env.fromCollection(in)

    pipeline(stream)
      .addSink(new SinkFunction[OUT] {
        override def invoke(in: OUT) = {
          companion.testResults += in
        }
      })

    env.execute(this.getClass.getSimpleName)

    val expectedResult = mutable.MutableList[OUT](out: _*)

    expectedResult shouldBe companion.testResults
  }

}

trait FlinkConnectedPipelineTestKit[IN1, IN2, OUT]
    extends StreamingMultipleProgramsTestBase
    with WordSpecLike
    with Matchers {

  def run[IN1: TypeInformation, IN2: TypeInformation](
      in1: Seq[IN1],
      in2: Seq[IN2],
      out: Seq[OUT],
      companion: FlinkTestKitCompanion[OUT])(pipeline: (DataStream[IN1], DataStream[IN2]) => DataStream[OUT]) = {

    companion.testResults = mutable.MutableList[OUT]()

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    val stream1 = env.fromCollection(in1)

    val stream2 = env.fromCollection(in2)

    pipeline(stream1, stream2)
      .addSink(new SinkFunction[OUT] {
        override def invoke(in: OUT) = {
          companion.testResults += in
        }
      })

    env.execute(this.getClass.getSimpleName)

    val expectedResult = mutable.MutableList[OUT](out: _*)

    expectedResult shouldBe companion.testResults
  }

}
