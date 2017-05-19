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

object PmmlModel {

  private val evaluatorInstance: ModelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

  private[api] def fromReader(reader: ModelReader): PmmlModel = {
    val readerFromFs = reader.buildDistributedPath
    val result = fromFilteredSource(readerFromFs)

    new PmmlModel(evaluatorInstance.newModelEvaluator(JAXBUtil.unmarshalPMML(result)))
  }

  private def fromFilteredSource(PMMLPath: String) = {
    JAXBUtil.createFilteredSource(new InputSource(new StringReader(PMMLPath)), new ImportFilter())
  }

}
class PmmlModel(private[api] val evaluator: Evaluator) extends Pipeline {

  import io.radicalbit.flink.pmml.scala.api.converter.VectorConverter._

  /**
    *
    * @param inputVector
    * @param replaceNan
    * @tparam V
    * @return
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

  /**
    * Contains the logic to validate an input Map and wrap it in the dedicated PMML classes.
    * @param input The stream input vector
    * @return
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

  private[api] def validateInput(v: Vector)(implicit vec2Pmml: (Vector, Evaluator) => PmmlInput): PmmlInput = {
    val modelSize = evaluator.getActiveFields.size

    if (v.size != modelSize)
      throw new InputValidationException(s"input vector $v size ${v.size} is not conform to model size $modelSize")
    else
      vec2Pmml(v, evaluator)
  }

  /** Used to evaluate an input against a PMML model. It returns the raw output of the JPMML evaluator
    *
    * @param preparedInput
    * @return
    */
  private[api] def evaluateInput(preparedInput: Map[FieldName, FieldValue]): util.Map[FieldName, _] =
    evaluator.evaluate(preparedInput)

  private[api] def extractTarget(evaluationResult: java.util.Map[FieldName, _]): Double = {
    val targets = extractTargetFields(evaluationResult)

    Option(targets.head._2) match {
      case Some(target) => extractTargetValue(target)
      case None => throw new JPMMLExtractionException("Target value is null.")
    }
  }

}
