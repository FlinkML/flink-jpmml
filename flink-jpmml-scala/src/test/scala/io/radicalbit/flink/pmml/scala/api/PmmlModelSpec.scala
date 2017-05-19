/*
 * flink-jpmml
 * Copyright (C) 2017 Radicalbit

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.radicalbit.flink.pmml.scala.api

import io.radicalbit.flink.pmml.scala.{InputPreparationException, InputValidationException, JPMMLExtractionException}
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.models.{Prediction, Score, Target}
import io.radicalbit.flink.pmml.scala.utils.{PmmlEvaluatorKit, PmmlLoaderKit}
import org.apache.flink.ml.math.{DenseVector, SparseVector}
import org.dmg.pmml.MiningField.UsageType
import org.dmg.pmml.{DataField, DataType, FieldName, InvalidValueTreatmentMethod, MiningField, OpType, OutputField}
import org.jpmml.evaluator.{FieldValueUtil, TargetField}
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success}

class PmmlModelSpec extends WordSpec with Matchers with PmmlLoaderKit with PmmlEvaluatorKit {

  import io.radicalbit.flink.pmml.scala.api.converter.VectorConverter._

  private val evaluator = buildEvaluator(getPMMLResource(Source.KmeansPmml))
  private val evaluatorNoOutput = buildEvaluator(getPMMLResource(Source.KmeansPmmlNoOut))
  private val evaluatorStrings = buildEvaluator(getPMMLResource(Source.KmeansPmmlStringFields))

  private val model = new PmmlModel(evaluator)
  private val modelStrings = new PmmlModel(evaluatorStrings)
  private val modelNoOutput = new PmmlModel(evaluatorNoOutput)

  "PmmlModel" should {

    "return correct output on dense input" in {
      val in = DenseVector(1.0, 1.0, 1.0, 1.0)
      val out = Prediction(Score(3.0))

      model.predict(in, None) shouldBe out
    }

    "return correct output on sparse input" in {
      val in = SparseVector(4, Array(0, 1, 2, 3), Array(1.0, 2.0, 3.0, 4.0))
      val out = Prediction(Score(4.0))

      model.predict(in, None) shouldBe out
    }

    "return correct output on sparse input with value replace" in {
      val in = SparseVector(4, Array(0, 2), Array(1.0, 2.0))
      val out = Prediction(Score(3.0))

      model.predict(in, Some(0.0)) shouldBe out
    }

    "return correct output on sparse input with value replace delegated to PMML" in {
      val in = SparseVector(4, Array(0, 2), Array(1.0, 2.0))
      val out = Prediction(Score(3.0))

      model.predict(in, None) shouldBe out
    }

    "empty output prediction if the input is not valid" in {
      val in = DenseVector(1.0, 2.0, 3.0)
      val out = Prediction(Target.empty)

      model.predict(in, None) shouldBe out
    }

  }

  "PmmlModel.prepareInput" should {

    "return a Map[FieldName,FieldValue] from a trivial inputMap" in {
      val inputVector = DenseVector(1.0, 1.0, 1.0, 1.0)
      val inputMap = buildExpectedInputMap(inputVector, evaluator.getActiveFields.map(_.getName.getValue))
      val result = model.prepareInput(inputMap, None)

      result shouldBe buildExpectedPreparedMap(inputMap, evaluator.getActiveFields.map(_.getName.getValue), None)
    }

    "return a Map[FieldName, FieldValue] from a non-trivial inputMap" in {
      val inputVector = DenseVector(2.0, -1.0, 3.0, 2.0)
      val inputMap = buildExpectedInputMap(inputVector, evaluator.getActiveFields.map(_.getName.getValue))
      val result = model.prepareInput(inputMap, None)

      result shouldBe buildExpectedPreparedMap(inputMap, evaluator.getActiveFields.map(_.getName.getValue), None)
    }

    "return a Map[FieldName, FieldValue] from a trivial SparseVector" in {
      val inputVector = SparseVector(4, Array(0, 1, 2, 3), Array(1.0, 2.0, 3.0, 4.0))
      val inputMap = buildExpectedInputMap(inputVector, evaluator.getActiveFields.map(_.getName.getValue))
      val result = model.prepareInput(inputMap, None)

      result shouldBe buildExpectedPreparedMap(inputMap, evaluator.getActiveFields.map(_.getName.getValue), None)
    }

    "return a Map[FieldName, FieldValue] from a non-trivial SparseVector" in {
      val inputVector = SparseVector(4, Array(0, 2), Array(1.0, 2.0))
      val inputMap = buildExpectedInputMap(inputVector, evaluator.getActiveFields.map(_.getName.getValue))
      val result = model.prepareInput(inputMap, None)

      result shouldBe buildExpectedPreparedMap(inputMap, evaluator.getActiveFields.map(_.getName.getValue), None)
    }

    "return a Map[FieldName, FieldValue] from a non-trivial SparseVector with replace value if specified" in {
      val inputVector = SparseVector(4, Array(0, 2), Array(1.0, 2.0))
      val inputMap = buildExpectedInputMap(inputVector, evaluator.getActiveFields.map(_.getName.getValue))
      val result = model.prepareInput(inputMap, Some(0.0))

      result shouldBe buildExpectedPreparedMap(inputMap, evaluator.getActiveFields.map(_.getName.getValue), Some(0.0))
    }

    "throw a InputPreparationException if the fields can't be prepared correctly" in {
      val inputVector = DenseVector(1.0, 4.0, -1.0, 3.0)
      val inputMap = buildExpectedInputMap(inputVector, evaluatorStrings.getActiveFields.map(_.getName.getValue))

      an[InputPreparationException] should be thrownBy modelStrings.prepareInput(inputMap, None)
    }

  }

  "PmmlModel.validateInput" should {

    "return a Map[String, Any] from a trivial DenseVector" in {
      val inputVector = DenseVector(1.0, 1.0, 1.0, 1.0)
      val result: Map[String, Any] = model.validateInput(inputVector)
      result shouldBe buildExpectedInputMap(inputVector, evaluator.getActiveFields.map(_.getName.getValue))
    }

    "return a Map[String,Any] from a non-trivial DenseVector" in {
      val inputVector = DenseVector(2.0, -1.0, 3.0, 2.0)
      val result = model.validateInput(inputVector)
      result shouldBe buildExpectedInputMap(inputVector, evaluator.getActiveFields.map(_.getName.getValue))
    }

    "return a Map[String, Any] from a trivial SparseVector" in {
      val inputVector = SparseVector(4, Array(0, 1, 2, 3), Array(1.0, 2.0, 3.0, 4.0))
      val result = model.validateInput(inputVector)
      result shouldBe buildExpectedInputMap(inputVector, evaluator.getActiveFields.map(_.getName.getValue))
    }

    "return a Map[String, Any] from a non-trivial SparseVector" in {
      val inputVector = SparseVector(4, Array(0, 2), Array(1.0, 2.0))
      val result = model.validateInput(inputVector)
      result shouldBe buildExpectedInputMap(inputVector, evaluator.getActiveFields.map(_.getName.getValue))
    }

    "throw a InputValidationException if the dense input vector is not valid (exceed model dimension)" in {
      val inputVector = DenseVector(1.0, 3.0, 2.0, 4.0, 5.0)
      an[InputValidationException] should be thrownBy model.validateInput(inputVector)
    }

    "throw a InputValidationException if the sparse input vector is not valid (exceed model dimension)" in {
      val inputVector = SparseVector(5, Array(0, 1, 2, 3), Array(1.0, 2.0, 3.0, 4.0))
      an[InputValidationException] should be thrownBy model.validateInput(inputVector)
    }

    "throw a InputValidationException if the dense input vector is not valid (not reach model dimension)" in {
      val inputVector = DenseVector(1.0, 3.0)
      an[InputValidationException] should be thrownBy model.validateInput(inputVector)
    }

  }

  "PmmlModel.extractTarget" should {

    "extract correctly String target" in {

      val outputField = buildOutputField("PCluster", OpType.CATEGORICAL, DataType.STRING)
      val miningField = buildMiningField("clazz", MiningField.UsageType.PREDICTED, InvalidValueTreatmentMethod.AS_IS)
      val dataField = buildDataField("clazz", OpType.CATEGORICAL, DataType.STRING)

      val target = new TargetField(dataField, miningField, evaluator.getTargetField.getTarget)

      val result: Map[FieldName, _] = Map(
        outputField.getName -> outputField,
        target.getName -> "3.0"
      )

      val extractedResult = model.extractTarget(result)

      extractedResult shouldBe 3.0

    }

    "extract correctly Double target" in {

      val outputField = buildOutputField("PCluster", OpType.CATEGORICAL, DataType.STRING)
      val miningField = buildMiningField("clazz", MiningField.UsageType.PREDICTED, InvalidValueTreatmentMethod.AS_IS)
      val dataField = buildDataField("clazz", OpType.CONTINUOUS, DataType.DOUBLE)

      val target = new TargetField(dataField, miningField, evaluator.getTargetField.getTarget)

      val result: Map[FieldName, _] = Map(
        outputField.getName -> outputField,
        target.getName -> 3.0
      )

      val extractedResult = model.extractTarget(result)

      extractedResult shouldBe 3.0

    }

    "throw extraction exception if no target is present" in {
      val outputField = buildOutputField("PCluster", OpType.CATEGORICAL, DataType.STRING)

      val result: Map[FieldName, _] = Map(outputField.getName -> outputField)

      an[JPMMLExtractionException] shouldBe thrownBy {
        model.extractTarget(result)
      }

    }

  }

  "PmmlModel.extractOutputFields" should {

    "return right output fields" in {
      val evaluationResultTest = buildExpectedPreparedMap(
        buildExpectedInputMap(DenseVector(1.0, 1.0, 1.0, 1.0), evaluator.getActiveFields.map(_.getName.getValue)),
        evaluator.getActiveFields.map(_.getName.getValue),
        None
      )
      val result = model.extractOutputFields(evaluationResultTest)
      assert(result.toMap.keys == Set("PCluster"))
    }

    "return an empty Map if no output field are present" in {
      val evaluationResultTest = buildExpectedPreparedMap(
        buildExpectedInputMap(DenseVector(1.0, 1.0, 1.0, 1.0),
                              evaluatorNoOutput.getActiveFields.map(_.getName.getValue)),
        evaluatorNoOutput.getActiveFields.map(_.getName.getValue),
        None
      )
      val result = modelNoOutput.extractOutputFields(evaluationResultTest)
      assert(result.toMap.keys == Set())
    }

  }

  "PmmlModel.extractTargetFields" should {

    "return the right target fields" in {
      val evaluationResultTest = buildExpectedPreparedMap(
        buildExpectedInputMap(DenseVector(1.0, 1.0, 1.0, 1.0), evaluator.getActiveFields.map(_.getName.getValue)),
        evaluator.getActiveFields.map(_.getName.getValue),
        None
      )
      val result = model.extractTargetFields(evaluationResultTest)
      assert(result.toMap.keys == Set("clazz"))
    }

  }

  "Pipeline.prepareAndEmit" should {

    "emit valid prepared value if the input is correct" in {
      val v = FieldValueUtil.create(null.asInstanceOf[DataType], null.asInstanceOf[OpType], 1.0)
      val d = createDataField()
      model.prepareAndEmit(Success(v), d.getName) shouldBe (d.getName, v)
    }

    "throw a InputPreparationException if the input field can't be prepared correctly" in {
      val d = createDataField()
      an[InputPreparationException] should be thrownBy model.prepareAndEmit(Failure(new Exception()), d.getName)
    }
  }

  "Singleton Loader" should {

    "load correctly a pmml model" in {
      noException should be thrownBy PmmlModel.fromReader(ModelReader(getPMMLSource(Source.KmeansPmml)))
    }

    "load correctly a pmml model (v4.2)" in {
      noException should be thrownBy PmmlModel.fromReader(ModelReader(getPMMLSource(Source.KmeansPmml42)))
    }

    "load correctly a pmml model (v4.1)" in {
      noException should be thrownBy PmmlModel.fromReader(ModelReader(getPMMLSource(Source.KmeansPmml41)))
    }

    "load correctly a pmml model (v4.0)" in {
      noException should be thrownBy PmmlModel.fromReader(ModelReader(getPMMLSource(Source.KmeansPmml40)))
    }

    "load correctly a pmml model (v3.2)" in {
      noException should be thrownBy PmmlModel.fromReader(ModelReader(getPMMLSource(Source.KmeansPmml32)))
    }

    "throw a JobExecutionException on wrong pmml model path" in {
      a[NullPointerException] should be thrownBy PmmlModel.fromReader(
        ModelReader(getPMMLSource(Source.NotExistingPath)))
    }

  }

  private def buildDataField(name: String, opType: OpType, dataType: DataType) = {
    val dataField = new DataField()
    dataField
      .setName(new FieldName(name))
      .setOpType(opType)
      .setDataType(dataType)
  }

  private def buildMiningField(name: String, usageType: UsageType, invalidTreat: InvalidValueTreatmentMethod) = {
    val miningField = new MiningField()
    miningField
      .setName(new FieldName(name))
      .setUsageType(usageType)
      .setInvalidValueTreatment(invalidTreat)
  }

  private def buildOutputField(name: String, opType: OpType, dataType: DataType) = {
    val output = new OutputField()
    output
      .setDisplayName(name)
      .setName(new FieldName(name))
      .setDataType(dataType)
      .setOpType(opType)
  }

  private def createDataField(field_name: String = "field_1") = {
    val d = new DataField()
    d.setDisplayName(field_name)
      .setName(new FieldName(field_name))
      .setDataType(DataType.DOUBLE)
      .setOpType(OpType.CONTINUOUS)
  }

}
