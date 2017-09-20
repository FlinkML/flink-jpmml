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

import java.util.UUID

import io.radicalbit.flink.pmml.scala.api.exceptions.{ModelLoadingException, WrongModelIdFormat}
import io.radicalbit.flink.pmml.scala.api.{Evaluator, PmmlModel}
import io.radicalbit.flink.pmml.scala.models.control.{AddMessage, ServingMessage}
import io.radicalbit.flink.pmml.scala.models.core.{ModelId, ModelInfo}
import io.radicalbit.flink.pmml.scala.models.prediction.{Prediction, Score}
import io.radicalbit.flink.pmml.scala.utils.models.{BaseInput, DynamicInput}
import io.radicalbit.flink.pmml.scala.utils._
import org.apache.flink.api.scala.ClosureCleaner
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

import scala.collection.immutable

object EvaluationCoFunctionSpec extends FlinkTestKitCompanion[Prediction] {

  private def evaluationCoOperator(f: (DynamicInput, PmmlModel) => Prediction) =
    new EvaluationCoFunction[DynamicInput, ServingMessage, Prediction] {

      servingMetadata = immutable.Map.empty[ModelId, ModelInfo]

      override def processElement1(event: DynamicInput,
                                   ctx: CoProcessFunction[DynamicInput, ServingMessage, Prediction]#Context,
                                   out: Collector[Prediction]): Unit =
        out.collect(f(event, servingModels.getOrElse(event.modelId.hashCode, fromMetadata(event.modelId))))
    }

  private val defaultPrediction = Prediction(Score(1.0))

  private val operator = evaluationCoOperator((_: DynamicInput, _: PmmlModel) => defaultPrediction)

  def operatorFunction: (DynamicInput, PmmlModel) => Prediction = (_, _) => defaultPrediction

}

class EvaluationCoFunctionSpec
    extends FlinkSourcedPipelineTestKit[DynamicInput, ServingMessage, Prediction]
    with PmmlLoaderKit
    with PmmlEvaluatorKit {

  import EvaluationCoFunctionSpec._

  private val modelPath = getPMMLSource(Source.KmeansPmml)

  private def pipeline(events: DataStream[DynamicInput], control: DataStream[ServingMessage]) =
    events.withSupportStream(control).process(operator)

  val nameModel: String = UUID.randomUUID().toString
  val version = "1"
  val modelId: String = BaseInput.toIdentifier(nameModel, version)

  "EvaluationCoFunction" should {

    "be Serializable" in {
      noException should be thrownBy ClosureCleaner.clean(operator, checkSerializable = true)
    }

    "return expected behavior on given function" in {
      val eventsStream = Seq(
        (0L, DynamicInput(modelId, List(1.0, 4.0, 2.0), System.currentTimeMillis())),
        (2L, DynamicInput(modelId, List(2.0, -1.0, 0.0), System.currentTimeMillis()))
      )
      val controlStream: Seq[(Long, ServingMessage)] = Seq(
        (1L, AddMessage(nameModel, version.toLong, modelPath, System.currentTimeMillis())),
        (3L, AddMessage(nameModel, version.toLong, modelPath, System.currentTimeMillis()))
      )
      run(eventsStream, controlStream, Seq(defaultPrediction, defaultPrediction), EvaluationCoFunctionSpec)(pipeline)
    }

    "load successfully a model" in {

      val modelInstance = buildEvaluator(getPMMLResource(Source.KmeansPmml))

      operator.loadModel(getPMMLSource(Source.KmeansPmml)).evaluator === modelInstance
    }

    "restore successfully a model" in {

      val modelInstance = buildEvaluator(getPMMLResource(Source.KmeansPmml))

      operator.loadModel(getPMMLSource(Source.KmeansPmml)).evaluator.model === modelInstance
    }

    "raise exception on wrong model" in {

      an[ModelLoadingException] shouldBe thrownBy { operator.loadModel(Source.NotExistingPath) }
    }

  }

  "fromMetadata function" should {

    "return an EmptyEvaluator if the servingMetadata is empty" in {

      val randModelId = BaseInput.toIdentifier(UUID.randomUUID().toString, version)

      val emptyEvaluator = new PmmlModel(Evaluator.empty).evaluator

      evaluationCoOperator(operatorFunction).fromMetadata(randModelId).evaluator shouldBe emptyEvaluator

    }

    "throw a WrongIdModelFormat if the id model format is wrong" in {

      val wrongIdModel = UUID.randomUUID().toString

      a[WrongModelIdFormat] shouldBe thrownBy { evaluationCoOperator(operatorFunction).fromMetadata(wrongIdModel) }
    }

    "throw a WrongIdModelFormat if the id model format is wrong 2" in {

      val wrongIdModel = BaseInput.toIdentifier(UUID.randomUUID().toString, UUID.randomUUID().toString)

      a[WrongModelIdFormat] shouldBe thrownBy { evaluationCoOperator(operatorFunction).fromMetadata(wrongIdModel) }
    }

  }

}
