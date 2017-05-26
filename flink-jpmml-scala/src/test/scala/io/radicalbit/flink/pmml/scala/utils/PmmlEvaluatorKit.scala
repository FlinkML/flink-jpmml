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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
