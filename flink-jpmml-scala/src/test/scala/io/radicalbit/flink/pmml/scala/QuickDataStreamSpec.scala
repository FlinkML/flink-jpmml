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

import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.models.{EmptyScore, Prediction, Score, Target}
import io.radicalbit.flink.pmml.scala.utils.{FlinkPipelineTestKit, FlinkTestKitCompanion, PmmlLoaderKit}
import org.apache.flink.api.scala.ClosureCleaner
import org.apache.flink.ml.math.{DenseVector, SparseVector, Vector}
import org.apache.flink.runtime.client.JobExecutionException
import org.apache.flink.streaming.api.scala._

object QuickDataStreamSpec extends FlinkTestKitCompanion[(Prediction, Vector)]

class QuickDataStreamSpec extends FlinkPipelineTestKit[Vector, (Prediction, Vector)] with PmmlLoaderKit {

  private val defaultInput: Vector = DenseVector(1.0, 1.0, 1.0, 1.0)
  private val defaultSparseInput: Vector = SparseVector(4, Array(0, 1, 2, 3), Array(1.0, 1.0, 1.0, 1.0))

  private val defaultPrediction = (Prediction(Score(3.0)), defaultInput)
  private val sparsePrediction = (Prediction(Score(3.0)), defaultSparseInput)
  private val emptyPrediction = (Prediction(Target.empty), defaultInput)

  private def pipelineBuilder(source: Option[String]) = {
    val reader = ModelReader(source getOrElse getPMMLSource(Source.KmeansPmml))

    (in: DataStream[Vector]) =>
      in.evaluate(reader)
  }

  "QuickDataStream" should {

    "quick DataStream should be serializable" in {
      noException should be thrownBy ClosureCleaner.clean(pipelineBuilder(None), checkSerializable = true)
    }

    "return correct output sequence on heterogeneous input" in {
      val in: Seq[Vector] = Seq(defaultInput, defaultSparseInput)
      val out = Seq(defaultPrediction, sparsePrediction)
      run(in, out, QuickDataStreamSpec)(pipelineBuilder(None))
    }

    "compute quick prediction with any dense input vector" in {
      val in: Seq[Vector] = Seq(defaultInput)
      val out = Seq(defaultPrediction)

      run(in, out, QuickDataStreamSpec)(pipelineBuilder(None))
    }

    "compute quick predictions with any sparse input vector" in {
      val in: Seq[Vector] = Seq(defaultSparseInput)
      val out = Seq(sparsePrediction)

      run(in, out, QuickDataStreamSpec)(pipelineBuilder(None))
    }

    "throw JobExecutionException if the model path cannot be loaded" in {
      val invalidSource = Source.NotExistingPath

      an[JobExecutionException] should be thrownBy {
        run(Seq(defaultInput), Seq(defaultPrediction), QuickDataStreamSpec)(pipelineBuilder(Some(invalidSource)))
      }
    }

    "Emit empty prediction if the input is not valid" in {
      val shortInput: Vector = SparseVector(2, Array(0, 3), Array(1.0, 1.0))

      run(Seq(shortInput), Seq((Prediction(Target.empty), shortInput)), QuickDataStreamSpec)(pipelineBuilder(None))
    }

    "Emit empty prediction if the model is not valid" in {
      val invalidModelSource = getPMMLSource(Source.KmeansPmmlEmpty)

      an[JobExecutionException] should be thrownBy {
        run(Seq(defaultInput), Seq(emptyPrediction), QuickDataStreamSpec)(pipelineBuilder(Some(invalidModelSource)))
      }
    }

  }

}
