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
  *
  *
  */
sealed trait VectorConverter[T] extends Serializable {
  def serializeVector(v: T, eval: Evaluator): Map[String, Any]
}

private[api] object VectorConverter {

  /** Type class pattern entry-point: it deliveries right converter depending
    * on the input type (i.e. Dense or Sparse)
    *
    */
  private[api] implicit object VectorConversion extends VectorConverter[Vector] {

    /** Converts a [[Vector]] to the internal type by mapping PMML model fields to vector values.
      *
      * @param v Input vector instance as [[Vector]]
      * @param evaluator Evaluator instance as [[Evaluator]]
      * @return The converted instance (could be either `Dense` or `Sparse`
      */
    def serializeVector(v: Vector, evaluator: Evaluator): PmmlInput = v match {
      case denseVector: DenseVector => DenseVector2Map.serializeVector(denseVector, evaluator)
      case sparseVector: SparseVector => SparseVector2Map.serializeVector(sparseVector, evaluator)
    }

  }

  private[api] implicit object DenseVector2Map extends VectorConverter[DenseVector] {

    /** Converts a [[DenseVector]] to the internal type by mapping PMML model fields to vector values.
      *
      * @param v The input Dense Vector
      * @param evaluator The evaluator instance
      * @return The converted instance
      */
    def serializeVector(v: DenseVector, evaluator: Evaluator): PmmlInput = {
      val getNameInput = getKeyFromModel(evaluator)

      getNameInput.zip(v.data).toMap
    }
  }

  private[api] implicit object SparseVector2Map extends VectorConverter[SparseVector] {

    /** Converts a [[SparseVector]] to the internal type by mapping PMML model fields to vector values.
      * Note that only existing values will be mapped
      *
      * @param v The input Sparse vector
      * @param evaluator The evaluator instance
      * @return The converted instance
      */
    def serializeVector(v: SparseVector, evaluator: Evaluator): PmmlInput = {
      val getNameInput = getKeyFromModel(evaluator)

      getNameInput.zip(toDenseData(v)).collect { case (key, Some(value)) => (key, value) }.toMap
    }

    private def toDenseData(sparseVector: SparseVector): Seq[Option[Any]] =
      (0 to sparseVector.size).map { index =>
        if (sparseVector.indices.contains(index)) Some(sparseVector(index)) else None
      }

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

}
