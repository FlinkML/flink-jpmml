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

/** Provides implicit classes enriching Flink [[DataStream]] in order to compute evaluations
  * against streams.
  *
  */
package object scala {

  /** Enriches Flink [[DataStream]] with [[evaluate]] method
    *
    * {{{
    *   case class Input(values: Seq[Double])
    *   val inputStream = env.fromCollection(Seq(Input(Seq(1.0)), Input(Seq(3.0)))
    *   inputStream.evaluate(reader) { (event, model) =>
    *
    *   }
    * }}}
    *
    * @param stream The input stream
    * @tparam T The input stream inner type
    */
  implicit class RichDataStream[T: TypeInformation: ClassTag](stream: DataStream[T]) {

    def evaluate[R: TypeInformation](modelReader: ModelReader)(f: (T, PmmlModel) => R): DataStream[R] = {
      val abstractOperator = new EvaluationFunction[T, R](modelReader) {
        override def flatMap(value: T, out: Collector[R]): Unit = out.collect(f(value, evaluator))
      }

      stream.flatMap(abstractOperator)
    }

  }

  /** Enriches Flink [[DataStream]] with [[evaluate]] on FlinkML [[Vector]] input stream
    *
    * @param stream The input stream
    * @tparam V The input stream inner type; it is subclass of [[Vector]]
    */
  implicit class QuickDataStream[V <: Vector: TypeInformation: ClassTag](stream: DataStream[V]) {

    def evaluate(modelReader: ModelReader): DataStream[(Prediction, V)] =
      new RichDataStream[V](stream).evaluate(modelReader) { (vec, model) =>
        {
          val result: Prediction = model.predict(vec, None)
          (result, vec)
        }
      }

  }

  /** Models conformity failure between PMML model and input [[DataStream]]
    *
    * @param msg
    */
  private[scala] class InputValidationException(msg: String) extends Exception(msg)

  /** Models [[org.jpmml.evaluator.EvaluatorUtil.prepare()]] method failure
    *
    * @param msg
    */
  private[scala] class InputPreparationException(msg: String) extends Exception(msg)

  /** Models empty result from [[org.jpmml.evaluator.ModelEvaluator]] evaluation
    *
    * @param msg
    */
  private[scala] class JPMMLExtractionException(msg: String) extends Exception(msg)

  /** Models failure on loading PMML model from distributed system
    *
    * @param msg
    * @param throwable
    */
  private[scala] class ModelLoadingException(msg: String, throwable: Throwable)
      extends RuntimeException(msg, throwable)

}
