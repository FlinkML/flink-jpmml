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

package io.radicalbit.flink.pmml.scala

import io.radicalbit.flink.pmml.scala.api.PmmlModel
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.models.prediction.{Prediction, Score, Target}
import io.radicalbit.flink.pmml.scala.utils.models.{BaseInput, Input}
import io.radicalbit.flink.pmml.scala.utils.PmmlLoaderKit
import io.radicalbit.flink.streaming.spec.core.{FlinkPipelineTestKit, FlinkTestKitCompanion}
import org.apache.flink.api.scala.ClosureCleaner
import org.apache.flink.runtime.client.JobExecutionException
import org.apache.flink.streaming.api.scala._
import org.scalatest.{Matchers, WordSpecLike}

object RichDataStreamSpec extends FlinkTestKitCompanion[Prediction] {

  private val defaultDenseEvalFunction = { (in: Input, model: PmmlModel) =>
    model.predict(BaseInput.toDenseVector(in), None)
  }

}

class RichDataStreamSpec
    extends FlinkPipelineTestKit[Input, Prediction]
    with WordSpecLike
    with Matchers
    with PmmlLoaderKit {

  import RichDataStreamSpec._

  private implicit val companion = RichDataStreamSpec

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

      executePipeline(in)(pipelineBuilder(None)(evalFunction)) shouldBe out
    }

    "throw JobExecutionException if the model path cannot be loaded" in {
      val randomSource = Source.NotExistingPath

      an[JobExecutionException] should be thrownBy {
        executePipeline(Seq(defaultInput))(pipelineBuilder(Some(randomSource))(defaultDenseEvalFunction)) shouldBe Seq(
          defaultPrediction)
      }
    }

    "Emit empty prediction if the input is not valid" in {
      val evalFunction = { (in: Input, model: PmmlModel) =>
        model.predict(BaseInput.toSparseVector(in, 2), None)
      }
      executePipeline(Seq(Input(1.0, 3.0)))(pipelineBuilder(None)(evalFunction)) shouldBe Seq(emptyPrediction)
    }

    "Emit empty prediction if the model is not valid" in {
      val invalidModelSource = getPMMLSource(Source.KmeansPmmlEmpty)

      an[JobExecutionException] should be thrownBy {
        executePipeline(Seq(defaultInput))(pipelineBuilder(Some(invalidModelSource))(defaultDenseEvalFunction)) shouldBe Seq(
          emptyPrediction)
      }
    }

  }

}
