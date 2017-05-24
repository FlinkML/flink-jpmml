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

package io.radicalbit.flink.pmml.scala.api.converter

import io.radicalbit.flink.pmml.scala.utils.{PmmlEvaluatorKit, PmmlLoaderKit}
import org.apache.flink.ml.math.{DenseVector, SparseVector, Vector}
import org.dmg.pmml.Model
import org.jpmml.evaluator.ModelEvaluator
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConversions._

class VectorConverterSpec extends WordSpec with Matchers with PmmlEvaluatorKit with PmmlLoaderKit {

  import VectorConverter._

  private val evaluator = buildEvaluator(getPMMLResource(Source.KmeansPmml))

  private val modelKeys: Seq[String] = evaluator.getActiveFields.map(_.getName.getValue)

  private def implicitTestConverter(input: Vector, evaluator: ModelEvaluator[_ <: Model])(
      implicit f: (Vector, ModelEvaluator[_ <: Model]) => Map[String, Any]) = f(input, evaluator)

  "VectorConverter" should {

    "convert a DenseVector to Map[String, Double]" in {
      val inputVector = DenseVector(1.0, 2.0, -1.0, 0.2)
      val outputValues = inputVector.data

      implicitTestConverter(inputVector, evaluator) shouldBe modelKeys.zip(outputValues).toMap
    }

    "convert a trivial SparseVector to Map[String, Double]" in {
      val inputVector = SparseVector(4, Array(0, 1, 2, 3), Array(1.0, 2.0, 3.0, 4.0))
      val outputValues = inputVector.toDenseVector.data

      implicitTestConverter(inputVector, evaluator) shouldBe modelKeys.zip(outputValues).toMap
    }

    "convert a non-trivial SparseVector to Map[String, Double]" in {
      val inputVector = SparseVector(4, Array(0, 2), Array(1.0, 2.0))
      val outputValues = Array.fill(4)(None: Option[Double])

      inputVector.indices.foreach(index => outputValues(index) = Some(inputVector(index)))

      implicitTestConverter(inputVector, evaluator) shouldBe modelKeys
        .zip(outputValues)
        .collect { case (fieldKey, Some(fieldValue)) => (fieldKey, fieldValue) }
        .toMap

    }

    "convert a short DenseVector to incomplete Map[String, Double]" in {
      val inputVector = DenseVector(4.0, -1.0)
      val outputVector = inputVector.data

      implicitTestConverter(inputVector, evaluator) shouldBe modelKeys.zip(outputVector).toMap
    }

    "return a key for binding map" in {
      modelKeys shouldBe Seq("sepal_length", "sepal_width", "petal_length", "petal_width")
    }

  }

}
