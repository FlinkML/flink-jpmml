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

import java.io.StringReader
import java.util

import io.radicalbit.flink.pmml.scala.{InputValidationException, JPMMLExtractionException}
import io.radicalbit.flink.pmml.scala.api.pipeline.Pipeline
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.models._
import org.apache.flink.ml.math.Vector
import org.dmg.pmml.FieldName
import org.jpmml.evaluator._
import org.jpmml.model.{ImportFilter, JAXBUtil}
import org.xml.sax.InputSource

import scala.collection.JavaConversions._
import scala.util.Try

/** Contains [[PmmlModel.fromReader]] factory method; as singleton, it guarantees one and only copy of the
  * model when the latter is requested.
  *
  */
object PmmlModel {

  private val evaluatorInstance: ModelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

  /** Loads the distributed path by [[io.radicalbit.flink.pmml.scala.api.reader.FsReader.buildDistributedPath]]
    * and construct a [[PmmlModel]] instance starting from `evalauatorInstance`
    *
    * @param reader
    * @return
    */
  private[api] def fromReader(reader: ModelReader): PmmlModel = {
    val readerFromFs = reader.buildDistributedPath
    val result = fromFilteredSource(readerFromFs)

    new PmmlModel(evaluatorInstance.newModelEvaluator(JAXBUtil.unmarshalPMML(result)))
  }

  private def fromFilteredSource(PMMLPath: String) = {
    JAXBUtil.createFilteredSource(new InputSource(new StringReader(PMMLPath)), new ImportFilter())
  }

}

/** Provides to the user the model instance and its methods; it is provided to the user as input of evaluate UDF
  *
  * {{{
  *   val toEvaluateStream: DataStream[Input]
  *   val reader: ModelReader
  *   toEvaluateStream.evaluate(reader) { (input, model) =>
  *     val pmmlModel: PmmlModel = model
  *     val prediction = model.evaluate(vectorized(input))
  *   }
  * }}}
  *
  * and it contains prediction method. It implements the core logic of the project.
  *
  * @param evaluator The PMML model instance
  */
class PmmlModel(private[api] val evaluator: Evaluator) extends Pipeline {

  import io.radicalbit.flink.pmml.scala.api.converter.VectorConverter._

  /** Implements the entire prediction pipeline, which can be described as 4 main steps:
    *
    * - validates the input to be conform to PMML model size [[PmmlModel.validateInput]]
    * - prepares the input in full compliance to [[EvaluatorUtil.prepare]] JPMML method [[PmmlModel.prepareInput]]
    * - evaluates the input against inner PMML model instance and returns a [[util.Map]]
    * output [[PmmlModel.evaluateInput]]
    * - extracts the target from evaluation result.
    *
    * As final action the [[Try]] statement is executed by [[Prediction.extractPrediction]] method.
    *
    * @param inputVector the input event as a [[Vector]] instance
    * @param replaceNan An [[Option]] describing a replace value for not defined vector values
    * @tparam V subclass of [[Vector]]
    * @return [[Prediction]] instance
    */
  final def predict[V <: Vector](inputVector: V, replaceNan: Option[Double] = None): Prediction = {
    val result = Try {
      val validatedInput = validateInput(inputVector)
      val preparedInput = prepareInput(validatedInput, replaceNan)
      val evaluationResult = evaluateInput(preparedInput)
      val extractResult = extractTarget(evaluationResult)
      extractResult
    }

    Prediction.extractPrediction(result)
  }

  /** Validates the input vector in size terms and converts it as a `Map[String, Any] (see [[PmmlInput]])
    *
    * @param v The input vector
    * @param vec2Pmml The conversion function
    * @return
    */
  private[api] def validateInput(v: Vector)(implicit vec2Pmml: (Vector, Evaluator) => PmmlInput): PmmlInput = {
    val modelSize = evaluator.getActiveFields.size

    if (v.size != modelSize)
      throw new InputValidationException(s"input vector $v size ${v.size} is not conform to model size $modelSize")
    else
      vec2Pmml(v, evaluator)
  }

  /** Binds each field with input value and prepare the record to be evaluated
    * by [[EvaluatorUtil.prepare]] method.
    *
    * @param input validated input as a [[Map]] keyed by field name
    * @param replaceNaN optional replace value in case of missing values
    * @return prepared input to be evaluated
    */
  private[api] def prepareInput(input: PmmlInput, replaceNaN: Option[Double]): Map[FieldName, FieldValue] = {

    val activeFields = evaluator.getActiveFields

    activeFields
      .map(field => {
        val rawValue = input.get(field.getName.getValue).orElse(replaceNaN).orNull
        prepareAndEmit(Try { EvaluatorUtil.prepare(field, rawValue) }, field.getName)
      })
      .toMap
  }

  /** Evaluates the prepared input against the PMML model
    *
    * @param preparedInput JPMML prepared input as `Map[FieldName, FieldValue]`
    * @return JPMML output result as a Java map
    */
  private[api] def evaluateInput(preparedInput: Map[FieldName, FieldValue]): util.Map[FieldName, _] =
    evaluator.evaluate(preparedInput)

  /** Extracts the target from evaluation result
    * @throws JPMMLExtractionException if the target couldn't be extracted
    * @param evaluationResult outcome from JPMML evaluation
    * @return The prediction value as a [Double]
    */
  private[api] def extractTarget(evaluationResult: java.util.Map[FieldName, _]): Double = {
    val targets = extractTargetFields(evaluationResult)

    Option(targets.head._2) match {
      case Some(target) => extractTargetValue(target)
      case None => throw new JPMMLExtractionException("Target value is null.")
    }
  }

}
