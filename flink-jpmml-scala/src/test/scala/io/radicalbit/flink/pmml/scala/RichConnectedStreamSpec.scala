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

import java.util.UUID

import io.radicalbit.flink.pmml.scala.api.PmmlModel
import io.radicalbit.flink.pmml.scala.models.control.{AddMessage, DelMessage, ServingMessage}
import io.radicalbit.flink.pmml.scala.models.prediction.{Prediction, Score, Target}
import io.radicalbit.flink.pmml.scala.utils.{FlinkSourcedPipelineTestKit, PmmlLoaderKit}
import io.radicalbit.flink.pmml.scala.utils.models.{BaseInput, DynamicInput}
import io.radicalbit.flink.streaming.spec.core.FlinkTestKitCompanion
import org.apache.flink.runtime.client.JobExecutionException
import org.apache.flink.streaming.api.scala._
import org.scalatest.{Matchers, WordSpecLike}

object RichConnectedStreamSpec extends FlinkTestKitCompanion[Prediction] {

  private val defaultDenseEvalFunction: (DynamicInput, PmmlModel) => Prediction = {
    (in: DynamicInput, model: PmmlModel) =>
      model.predict(BaseInput.toDenseVector(in), None)
  }

}

class RichConnectedStreamSpec
    extends FlinkSourcedPipelineTestKit[DynamicInput, ServingMessage, Prediction]
    with WordSpecLike
    with Matchers
    with PmmlLoaderKit {

  import RichConnectedStreamSpec._

  private implicit val companion = RichConnectedStreamSpec

  private val defaultPrediction = Prediction(Score(3.0))
  private val emptyPrediction = Prediction(Target.empty)

  private def pipeline(inputStream: DataStream[DynamicInput], controlStream: DataStream[ServingMessage]) =
    inputStream
      .withSupportStream(controlStream)
      .evaluate(defaultDenseEvalFunction)

  val nameModel1 = UUID.randomUUID().toString
  val nameModel2 = UUID.randomUUID().toString

  val version = "1"

  val idModel1 = BaseInput.toIdentifier(nameModel1, version)
  val idModel2 = BaseInput.toIdentifier(nameModel2, version)

  "RichConnectedStreamSpec" should {

    "compute the right prediction (-> model -> event = prediction)" in {

      val in1 = Seq((1L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), 0)))

      val in2: Seq[(Long, ServingMessage)] =
        Seq((0L, AddMessage(nameModel1, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis())))

      val out = Seq(defaultPrediction)

      executePipeline(in1, in2)(pipeline) shouldBe out
    }

    "compute the right prediction (-> event -> model -> event = empty prediction, default prediction)" in {

      val in1 = Seq(
        (0L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), 0)),
        (2L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), 2))
      )
      val in2: Seq[(Long, ServingMessage)] =
        Seq((1L, AddMessage(nameModel1, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis())))

      val out = Seq(emptyPrediction, defaultPrediction)

      executePipeline(in1, in2)(pipeline) shouldBe out
    }

    "compute the right prediction (-> model1 -> model2 -> event1 -> event2 = two predictions)" in {

      val in1 = Seq(
        (2L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())),
        (3L, DynamicInput(idModel2, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis()))
      )

      val in2: Seq[(Long, ServingMessage)] = Seq(
        (0L, AddMessage(nameModel1, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis())),
        (1L,
         AddMessage(nameModel2,
                    version.toLong,
                    getPMMLSource(Source.KmeansPmmlNoOutNoTrg),
                    System.currentTimeMillis()))
      )

      val out = Seq(defaultPrediction, emptyPrediction)

      executePipeline(in1, in2)(pipeline) shouldBe out
    }

    "return the correct predictions (-> event1 -> model1 -> event2 -> model2 -> event1 -> event2 = two empty predictions and two predictions)" in {

      val in1 = Seq(
        (0L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())),
        (2L, DynamicInput(idModel2, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())),
        (4L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())),
        (5L, DynamicInput(idModel2, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis()))
      )
      val in2: Seq[(Long, ServingMessage)] = Seq(
        (1L, AddMessage(nameModel1, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis())),
        (3L, AddMessage(nameModel2, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis()))
      )

      val output = Seq(
        emptyPrediction,
        emptyPrediction,
        defaultPrediction,
        defaultPrediction
      )

      executePipeline(in1, in2)(pipeline) shouldBe output
    }

    "return the correct predictions with only events coming" in {

      val in1 = Seq(
        (0L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())),
        (1L, DynamicInput(idModel2, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis()))
      )

      val in2: Seq[(Long, ServingMessage)] = Seq.empty

      val out = Seq(emptyPrediction, emptyPrediction)

      executePipeline(in1, in2)(pipeline) shouldBe out
    }

    "return the correct predictions with only models coming" in {

      val in1 = Seq.empty[(Long, DynamicInput)]

      val in2: Seq[(Long, ServingMessage)] = Seq(
        (0L, AddMessage(nameModel1, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis())),
        (1L,
         AddMessage(nameModel2,
                    version.toLong,
                    getPMMLSource(Source.KmeansPmmlNoOutNoTrg),
                    System.currentTimeMillis()))
      )

      val out = Seq.empty[Prediction]

      executePipeline(in1, in2)(pipeline) shouldBe out

    }

    "return EmptyScore if a DelMessage delete with no models" in {

      val in1: Seq[(Long, DynamicInput)] =
        Seq((1L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())))

      val in2: Seq[(Long, ServingMessage)] =
        Seq((0L, DelMessage(nameModel1, version.toLong, System.currentTimeMillis())))

      val out = Seq(emptyPrediction)

      executePipeline(in1, in2)(pipeline) shouldBe out
    }

    "return EmptyScore if a DelMessage delete the current model" in {

      val in1: Seq[(Long, DynamicInput)] =
        Seq(
          (1L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())),
          (3L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis()))
        )

      val in2: Seq[(Long, ServingMessage)] =
        Seq(
          (0L, AddMessage(nameModel1, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis())),
          (2L, DelMessage(nameModel1, version.toLong, System.currentTimeMillis()))
        )

      val out = Seq(defaultPrediction, emptyPrediction)

      executePipeline(in1, in2)(pipeline) shouldBe out
    }

    "return Prediction if a DelMessage delete a not existing model" in {

      val in1: Seq[(Long, DynamicInput)] =
        Seq(
          (1L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())),
          (3L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis()))
        )

      val in2: Seq[(Long, ServingMessage)] =
        Seq(
          (0L, AddMessage(nameModel1, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis())),
          (2L, DelMessage(nameModel2, version.toLong, System.currentTimeMillis()))
        )

      val out = Seq(defaultPrediction, defaultPrediction)

      executePipeline(in1, in2)(pipeline) shouldBe out
    }

    "return Prediction on Add --> Delete --> Add pattern" in {

      val in1: Seq[(Long, DynamicInput)] =
        Seq(
          (1L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())),
          (3L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())),
          (5L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis()))
        )

      val in2: Seq[(Long, ServingMessage)] =
        Seq(
          (0L, AddMessage(nameModel1, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis())),
          (2L, DelMessage(nameModel1, version.toLong, System.currentTimeMillis())),
          (4L, AddMessage(nameModel1, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis()))
        )

      val out = Seq(defaultPrediction, emptyPrediction, defaultPrediction)

      executePipeline(in1, in2)(pipeline) shouldBe out
    }

    "return Prediction on Delete --> Add --> Delete pattern" in {

      val in1: Seq[(Long, DynamicInput)] =
        Seq(
          (1L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())),
          (3L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())),
          (5L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis()))
        )

      val in2: Seq[(Long, ServingMessage)] =
        Seq(
          (0L, DelMessage(nameModel1, version.toLong, System.currentTimeMillis())),
          (2L, AddMessage(nameModel1, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis())),
          (4L, DelMessage(nameModel1, version.toLong, System.currentTimeMillis()))
        )

      val out = Seq(emptyPrediction, defaultPrediction, emptyPrediction)

      executePipeline(in1, in2)(pipeline) shouldBe out
    }

    "discard ADD message on same Identifier (Add message with newer version needed here)." in {

      val in1 = Seq(
        (1L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())),
        (3L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis()))
      )

      val in2: Seq[(Long, ServingMessage)] = Seq(
        (0L, AddMessage(nameModel1, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis())),
        (2L,
         AddMessage(nameModel1,
                    version.toLong,
                    getPMMLSource(Source.KmeansPmmlNoOutNoTrg),
                    System.currentTimeMillis()))
      )

      val out = Seq(defaultPrediction, defaultPrediction)

      executePipeline(in1, in2)(pipeline) shouldBe out
    }

    "throw JobExecutionException if the model path cannot be loaded" in {

      val in1 = Seq((1L, DynamicInput(idModel1, List(1.0, 1.0, 1.0, 1.0), System.currentTimeMillis())))

      val in2: Seq[(Long, ServingMessage)] =
        Seq((0L, AddMessage(nameModel1, version.toLong, Source.NotExistingPath, System.currentTimeMillis())))

      val out = Seq(defaultPrediction)

      an[JobExecutionException] should be thrownBy {
        executePipeline(in1, in2)(pipeline) shouldBe out
      }
    }

    "Emit empty prediction if the input is not valid" in {
      val evalFunction = { (in: DynamicInput, model: PmmlModel) =>
        model.predict(BaseInput.toSparseVector(in, 2), None)
      }

      def customPipeline(inputStream: DataStream[DynamicInput], controlStream: DataStream[ServingMessage]) =
        inputStream
          .withSupportStream(controlStream)
          .evaluate(evalFunction)

      val in1 = Seq((1L, DynamicInput(idModel1, List(1.0, 1.0), System.currentTimeMillis())))

      val in2: Seq[(Long, ServingMessage)] =
        Seq((0L, AddMessage(nameModel1, version.toLong, getPMMLSource(Source.KmeansPmml), System.currentTimeMillis())))

      val out = Seq(emptyPrediction)

      executePipeline(in1, in2)(pipeline) shouldBe out
    }

    "Emit empty prediction if the model is not valid" in {
      val invalidModelSource = getPMMLSource(Source.KmeansPmmlEmpty)

      val in1 = Seq((1L, DynamicInput(idModel1, List(1.0, 1.0), System.currentTimeMillis())))

      val in2: Seq[(Long, ServingMessage)] =
        Seq((0L, AddMessage(nameModel1, version.toLong, invalidModelSource, System.currentTimeMillis())))

      val out = Seq(emptyPrediction)

      an[JobExecutionException] should be thrownBy {
        executePipeline(in1, in2)(pipeline) shouldBe out
      }
    }

  }

}
