package io.radicalbit.flink.pmml.scala.api.pipeline

import java.util

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
