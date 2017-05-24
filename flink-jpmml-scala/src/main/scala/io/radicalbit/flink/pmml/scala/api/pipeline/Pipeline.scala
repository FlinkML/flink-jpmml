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

/** Self type trait extending [[PmmlModel]] instance methods; they offer
  * additional features for the preparation and extraction pipeline steps.
  *
  */
private[api] trait Pipeline { self: PmmlModel =>

  def predict[V <: Vector](inputVector: V, replaceNan: Option[Double] = None): Prediction

  /** Emits prepared input if JPMML preparation went fine.
    *
    * @throws InputPreparationException if the JPMML preparation fails.
    *
    * @param outcome `Try` evaluation of [[EvaluatorUtil.prepare]] method output value
    * @param field The field name related to the value
    * @return Prepared Input
    */
  private[api] def prepareAndEmit(outcome: Try[FieldValue], field: FieldName): (FieldName, FieldValue) =
    outcome match {
      case Success(value) => (field, value)
      case Failure(_) =>
        throw new InputPreparationException(s"The ${field.getValue} field JPMML finalization failed.")
    }

  /** Extracts all the target fields specified by the PMML document.
    *
    * @param evaluationResult evaluation step outcome
    * @return extracted output
    */
  private[api] def extractTargetFields(evaluationResult: java.util.Map[FieldName, _]): Seq[(String, Any)] =
    extractFields(evaluator.getTargetFields, evaluationResult, evaluator)

  /** Extracts all the output fields specified by the PMML document.
    *
    * @param evaluationResult evaluation step outcome
    * @return extracted output
    */
  private[api] def extractOutputFields(evaluationResult: java.util.Map[FieldName, _]): Seq[(String, Any)] =
    extractFields(evaluator.getOutputFields, evaluationResult, evaluator)

  /** Calls [[EvaluatorUtil.decode]] for each field demanded for extraction.
    *
    * @param fields demanded for extraction
    * @param evaluationResult evaluation outcome container
    * @param evaluator The PMML instance as a [[org.jpmml.evaluator.ModelEvaluator]]
    * @return
    */
  private[api] def extractFields(fields: java.util.List[_ <: ModelField],
                                 evaluationResult: java.util.Map[FieldName, _],
                                 evaluator: Evaluator): mutable.Buffer[(String, AnyRef)] =
    for {
      field <- fields
      fieldName <- Option(field.getName)
    } yield { fieldName.getValue -> EvaluatorUtil.decode(evaluationResult.get(fieldName)) }

  /** Casts a String to Double if the outcome is a String, returns the Double otherwise.
    *
    * @param target The extracted target
    * @throws scala.ClassCastException if the outcome could not be casted to Double
    * @return The outcome as a Double
    */
  @throws(classOf[ClassCastException])
  protected def extractTargetValue(target: Any): Option[Double] = target match {
    case s: String => Some(s.toDouble)
    case d: Double => Some(d)
    case _ => None
  }

}
