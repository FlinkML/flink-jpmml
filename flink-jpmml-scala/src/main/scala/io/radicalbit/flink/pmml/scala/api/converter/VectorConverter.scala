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

package io.radicalbit.flink.pmml.scala.api.converter

import io.radicalbit.flink.pmml.scala.api.{Evaluator, PmmlInput}
import org.apache.flink.ml.math.{DenseVector, SparseVector, Vector}

import scala.collection.JavaConversions._

/** Type Class Pattern implementing converters from Flink [[org.apache.flink.ml.math.Vector]] instances to
  * internal types; the existing fields (i.e. value defined fields) are modeled as [[scala.collection.mutable.Map]]s; the
  * not existing fields (i.e. NaN values) will not be mapped within the Internal type
  */
sealed trait VectorConverter[-T] extends Serializable {
  def serializeVector(v: T, eval: Evaluator): Map[String, Any]
}

private[api] object VectorConverter {

  private def apply[A: VectorConverter] = implicitly[VectorConverter[A]]

  private def createConverter[IN](serialize: (IN, Evaluator) => PmmlInput): VectorConverter[IN] =
    new VectorConverter[IN] {
      override def serializeVector(v: IN, eval: Evaluator): PmmlInput =
        serialize(v, eval)
    }

  private[api] implicit val vectorConversion: VectorConverter[Vector] =
    createConverter {
      case (vec: DenseVector, evaluator) => denseVector2Map.serializeVector(vec, evaluator)
      case (vec: SparseVector, evaluator) => sparseVector2Map.serializeVector(vec, evaluator)
    }

  private[api] implicit val denseVector2Map: VectorConverter[DenseVector] =
    createConverter { (vec, evaluator) =>
      getKeyFromModel(evaluator)
        .zip(vec.data)
        .toMap
    }

  private[api] implicit val sparseVector2Map: VectorConverter[SparseVector] =
    createConverter { (vec, evaluator) =>
      getKeyFromModel(evaluator)
        .zip(toDenseData(vec))
        .collect { case (key, Some(value)) => (key, value) }
        .toMap
    }

  private[api] implicit def applyConversion[T: VectorConverter, E <: Evaluator](dataVector: T, evaluator: E) =
    implicitly[VectorConverter[T]].serializeVector(dataVector, evaluator)

  /** Extracts the key values of the model fields from [[Evaluator]] instance.
    *
    * @param evaluator PMML evaluator instance
    * @return the keys as a Scala [[Seq]]
    *
    */
  private def getKeyFromModel(evaluator: Evaluator) =
    evaluator.getActiveFields.map(_.getName.getValue)

  private def toDenseData(sparseVector: SparseVector): Seq[Option[Any]] =
    (0 to sparseVector.size).map { index =>
      if (sparseVector.indices.contains(index)) Some(sparseVector(index)) else None
    }

}
