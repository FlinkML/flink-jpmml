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

package io.radicalbit.flink.pmml.scala.utils.models

import io.radicalbit.flink.pmml.scala.models.core.ModelId
import io.radicalbit.flink.pmml.scala.models.input.BaseEvent
import org.apache.flink.ml.math.{DenseVector, SparseVector}

object BaseInput {

  def toDenseVector(input: BaseInput): DenseVector =
    DenseVector(input.values.toArray)

  def toSparseVector(input: BaseInput, size: Int): SparseVector =
    SparseVector(size, Array.range(0, input.size - 1), input.values.toArray)

  def toIdentifier(name: String, version: String): String = name + ModelId.separatorSymbol + version

}
sealed trait BaseInput {

  def values: Seq[Double]

  def size: Int
}

object Input {
  def apply(rawValues: Double*): Input = Input(values = rawValues.toList)
}
case class Input(values: List[Double]) extends BaseInput {
  def size: Int = values.size
}

final case class DynamicInput(modelId: String, values: List[Double], occurredOn: Long)
    extends BaseEvent
    with BaseInput {
  def size: Int = values.size
}
