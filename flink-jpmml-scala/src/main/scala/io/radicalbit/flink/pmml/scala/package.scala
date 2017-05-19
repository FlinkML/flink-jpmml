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

package io.radicalbit.flink.pmml

import io.radicalbit.flink.pmml.scala.api.PmmlModel
import io.radicalbit.flink.pmml.scala.api.functions.EvaluationFunction
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.models.Prediction
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.ml.math.Vector
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

import _root_.scala.reflect.ClassTag

package object scala {

  implicit class RichDataStream[T: TypeInformation: ClassTag](stream: DataStream[T]) {

    def evaluate[R: TypeInformation](modelReader: ModelReader)(f: (T, PmmlModel) => R): DataStream[R] = {
      val abstractOperator = new EvaluationFunction[T, R](modelReader) {
        override def flatMap(value: T, out: Collector[R]): Unit = out.collect(f(value, evaluator))
      }

      stream.flatMap(abstractOperator)
    }

  }

  implicit class QuickDataStream[V <: Vector: TypeInformation: ClassTag](stream: DataStream[V]) {

    def evaluate(modelReader: ModelReader): DataStream[(Prediction, V)] =
      new RichDataStream[V](stream).evaluate(modelReader) { (vec, model) =>
        {
          val result: Prediction = model.predict(vec, None)
          (result, vec)
        }
      }

  }

  private[scala] class InputValidationException(msg: String) extends Exception(msg)

  private[scala] class InputPreparationException(msg: String) extends Exception(msg)

  private[scala] class JPMMLExtractionException(msg: String) extends Exception(msg)

  private[scala] class ModelLoadingException(msg: String, throwable: Throwable)
      extends RuntimeException(msg, throwable)

}
