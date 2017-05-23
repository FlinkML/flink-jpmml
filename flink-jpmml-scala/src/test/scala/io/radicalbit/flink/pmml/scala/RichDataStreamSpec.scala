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

package io.radicalbit.flink.pmml.scala

import io.radicalbit.flink.pmml.scala.RichDataStreamSpec.Input
import io.radicalbit.flink.pmml.scala.api.PmmlModel
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.models.{Prediction, Score, Target}
import io.radicalbit.flink.pmml.scala.utils.{FlinkPipelineTestKit, FlinkTestKitCompanion, PmmlLoaderKit}
import org.apache.flink.api.scala.ClosureCleaner
import org.apache.flink.ml.math.{DenseVector, SparseVector}
import org.apache.flink.runtime.client.JobExecutionException
import org.apache.flink.streaming.api.scala._

object RichDataStreamSpec extends FlinkTestKitCompanion[Prediction] {

  object Input {
    def apply(rawValues: Double*): Input = Input(values = rawValues.toList)
  }
  case class Input(values: List[Double]) {
    def size: Int = values.size
  }

  private val defaultDenseEvalFunction = { (in: Input, model: PmmlModel) =>
    model.predict(toDenseVector(in), None)
  }
  private val defaultSparseEvalFunction = { (in: Input, model: PmmlModel) =>
    model.predict(toSparseVector(in, 4), None)
  }

  private def toDenseVector(input: Input): DenseVector =
    DenseVector(input.values.toArray)

  private def toSparseVector(input: Input, size: Int): SparseVector =
    SparseVector(size, Array.range(0, input.size - 1), input.values.toArray)

}

class RichDataStreamSpec extends FlinkPipelineTestKit[Input, Prediction] with PmmlLoaderKit {

  import RichDataStreamSpec._

  private val defaultInput = Input(1.0, 1.0, 1.0, 1.0)

  private val defaultPrediction = Prediction(Score(3.0))
  private val emptyPrediction = Prediction(Target.empty)

  private def pipelineBuilder(source: Option[String])(
      f: (Input, PmmlModel) => Prediction): DataStream[Input] => DataStream[Prediction] = {

    val evaluator = ModelReader(source getOrElse getPMMLSource(Source.KmeansPmml))

    (dataInput: DataStream[Input]) =>
      dataInput.evaluate(evaluator)(f)
  }

  "flink-jpmml" should {

    "richDataStream should be serializable" in {
      val evalFunction = defaultDenseEvalFunction

      noException should be thrownBy ClosureCleaner.clean(pipelineBuilder(None)(evalFunction))
    }

    "compute predictions with any input and an evaluation function" in {
      val in = Seq(defaultInput)
      val out = Seq(defaultPrediction)

      val evalFunction = defaultDenseEvalFunction

      run(in, out, RichDataStreamSpec)(pipelineBuilder(None)(evalFunction))
    }

    "throw JobExecutionException if the model path cannot be loaded" in {
      val randomSource = Source.NotExistingPath

      an[JobExecutionException] should be thrownBy {
        run(Seq(defaultInput), Seq(defaultPrediction), RichDataStreamSpec)(
          pipelineBuilder(Some(randomSource))(defaultDenseEvalFunction))
      }
    }

    "Emit empty prediction if the input is not valid" in {
      val evalFunction = { (in: Input, model: PmmlModel) =>
        model.predict(toSparseVector(in, 2), None)
      }
      run(Seq(Input(1.0, 3.0)), Seq(emptyPrediction), RichDataStreamSpec)(pipelineBuilder(None)(evalFunction))
    }

    "Emit empty prediction if the model is not valid" in {
      val invalidModelSource = getPMMLSource(Source.KmeansPmmlEmpty)

      an[JobExecutionException] should be thrownBy {
        run(Seq(defaultInput), Seq(emptyPrediction), RichDataStreamSpec)(
          pipelineBuilder(Some(invalidModelSource))(defaultDenseEvalFunction))
      }
    }

  }

}
