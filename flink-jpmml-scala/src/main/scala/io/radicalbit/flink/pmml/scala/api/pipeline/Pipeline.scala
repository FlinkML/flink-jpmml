package io.radicalbit.flink.pmml.scala.api.pipeline

import io.radicalbit.flink.pmml.scala.api.{InputPreparationException, PmmlModel}
import org.dmg.pmml.FieldName
import org.jpmml.evaluator.{Evaluator, EvaluatorUtil, FieldValue, ModelField}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

private[api] trait Pipeline { self: PmmlModel =>

  def prepareAndEmit(outcome: Try[FieldValue], field: FieldName): (FieldName, FieldValue) =
    outcome match {
      case Success(value) => (field, value)
      case Failure(_) =>
        throw new InputPreparationException("The " + field.getValue + " field JPMML finalization failed.")
    }

  protected def extractFields(fields: java.util.List[_ <: ModelField],
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
