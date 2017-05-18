package io.radicalbit.flink.pmml.scala.utils

import org.apache.flink.ml.math.{DenseVector, SparseVector, Vector}
import org.dmg.pmml.{FieldName, Model, PMML}
import org.jpmml.evaluator.{FieldValueUtil, ModelEvaluator, ModelEvaluatorFactory}

trait PmmlEvaluatorKit {

  final protected def buildEvaluator(pmml: PMML): ModelEvaluator[_ <: Model] =
    ModelEvaluatorFactory.newInstance.newModelEvaluator(pmml)

  final protected def buildExpectedInputMap(in: Vector, keys: Seq[String]) = {
    val data: Seq[Option[Double]] = in match {
      case dv: DenseVector => dv.data.map(Option(_))
      case sv: SparseVector => (0 to keys.size).map(index => if (sv.indices.contains(index)) Some(sv(index)) else None)
    }

    keys.zip(data).collect { case (k, Some(v)) => k -> v } toMap
  }

  final protected def buildExpectedPreparedMap(in: Map[String, Any], keys: Seq[String], replaceValue: Option[Double]) =
    keys.map {
      case k if in.contains(k) => new FieldName(k) -> FieldValueUtil.create(null, null, in(k))
      case emptyKey => new FieldName(emptyKey) -> FieldValueUtil.create(null, null, replaceValue.orNull)
    } toMap

}
