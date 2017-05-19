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

package io.radicalbit.flink.pmml.scala.api.pipeline

import io.radicalbit.flink.pmml.scala.InputPreparationException
import io.radicalbit.flink.pmml.scala.api._
import io.radicalbit.flink.pmml.scala.models.Prediction
import org.apache.flink.ml.math.Vector
import org.dmg.pmml.FieldName
import org.jpmml.evaluator.{EvaluatorUtil, FieldValue, ModelField}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

private[api] trait Pipeline { self: PmmlModel =>

  def predict[V <: Vector](inputVector: V, replaceNan: Option[Double] = None): Prediction

  private[api] def prepareAndEmit(outcome: Try[FieldValue], field: FieldName): (FieldName, FieldValue) =
    outcome match {
      case Success(value) => (field, value)
      case Failure(_) =>
        throw new InputPreparationException("The " + field.getValue + " field JPMML finalization failed.")
    }

  /**
    * Used to extract all the target fields specified by the PMML document.
    * @param evaluationResult
    * @return
    */
  private[api] def extractTargetFields(evaluationResult: java.util.Map[FieldName, _]): Seq[(String, Any)] =
    extractFields(evaluator.getTargetFields, evaluationResult, evaluator)

  /**
    * Used to extract all the output fields specified by the PMML document.
    * @param evaluationResult
    * @return
    */
  private[api] def extractOutputFields(evaluationResult: java.util.Map[FieldName, _]): Seq[(String, Any)] =
    extractFields(evaluator.getOutputFields, evaluationResult, evaluator)

  private[api] def extractFields(fields: java.util.List[_ <: ModelField],
                                 evaluationResult: java.util.Map[FieldName, _],
                                 evaluator: Evaluator): mutable.Buffer[(String, AnyRef)] =
    for {
      field <- fields
      fieldName <- Option(field.getName)
    } yield { fieldName.getValue -> EvaluatorUtil.decode(evaluationResult.get(fieldName)) }

  @throws(classOf[ClassCastException])
  protected def extractTargetValue(target: Any): Double = target match {
    case s: String => s.toDouble
    case d: Double => d
  }

}
