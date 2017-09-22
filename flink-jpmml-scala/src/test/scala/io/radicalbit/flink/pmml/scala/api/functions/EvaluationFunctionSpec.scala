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

package io.radicalbit.flink.pmml.scala.api.functions

import io.radicalbit.flink.pmml.scala.api.PmmlModel
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.models.prediction.{Prediction, Score}
import io.radicalbit.flink.pmml.scala.utils.models.Input
import io.radicalbit.flink.pmml.scala.utils.PmmlLoaderKit
import io.radicalbit.flink.streaming.spec.core.{FlinkPipelineTestKit, FlinkTestKitCompanion}
import org.apache.flink.api.scala.ClosureCleaner
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.scalatest.{Matchers, WordSpecLike}

object EvaluationFunctionSpec extends FlinkTestKitCompanion[Prediction]

class EvaluationFunctionSpec
    extends FlinkPipelineTestKit[Input, Prediction]
    with WordSpecLike
    with Matchers
    with PmmlLoaderKit {

  private implicit val companion = EvaluationFunctionSpec

  private val reader = ModelReader(getPMMLSource(Source.KmeansPmml))

  private def evaluationOperator[T](source: ModelReader)(f: (T, PmmlModel) => Prediction) =
    new EvaluationFunction[T, Prediction](source) {
      override def flatMap(value: T, out: Collector[Prediction]): Unit = out.collect(f(value, evaluator))
    }

  private val operator = evaluationOperator(reader) { (in: Input, model: PmmlModel) =>
    Prediction(Score(1.0))
  }

  private def pipeline(source: DataStream[Input]): DataStream[Prediction] = source.flatMap(operator)

  "EvaluationFunction" should {

    "be Serializable" in {
      noException should be thrownBy ClosureCleaner.clean(operator, checkSerializable = true)
    }

    "return expected behavior on given function" in {
      executePipeline(Seq(Input(1.0, 2.0)))(pipeline) shouldBe Seq(Prediction(Score(1.0)))
    }

  }

}
