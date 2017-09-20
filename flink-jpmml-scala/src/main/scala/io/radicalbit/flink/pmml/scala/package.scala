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

package io.radicalbit.flink.pmml

import io.radicalbit.flink.pmml.scala.api.PmmlModel
import io.radicalbit.flink.pmml.scala.api.functions.{EvaluationCoFunction, EvaluationFunction}
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.models.control.ServingMessage
import io.radicalbit.flink.pmml.scala.models.input.BaseEvent
import io.radicalbit.flink.pmml.scala.models.prediction.Prediction
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.ml.math.Vector
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

import _root_.scala.reflect.ClassTag

/** Main library package, it contains the core of the library.
  *
  * The `scala` package object provides implicit classes enriching Flink
  * [[org.apache.flink.streaming.api.scala.DataStream]] in order to compute evaluations against streams.
  *
  */
package object scala {

  /** Enriches Flink [[org.apache.flink.streaming.api.scala.DataStream]] with [[evaluate]] method, as
    *
    * {{{
    *   case class Input(values: Seq[Double])
    *   val inputStream = env.fromCollection(Seq(Input(Seq(1.0)), Input(Seq(3.0)))
    *   inputStream.evaluate(reader) { (event, model) =>
    *     val prediction = model.predict(event.toVector)
    *     prediction.value
    *   }
    * }}}
    *
    * @param stream The input stream
    * @tparam T The input stream inner Type
    */
  implicit class RichDataStream[T: TypeInformation: ClassTag](stream: DataStream[T]) {

    /**
      * It connects the main `DataStream` with the `ControlStream`
      */
    def withSupportStream[CTRL <: ServingMessage: TypeInformation](
        supportStream: DataStream[CTRL]): ConnectedStreams[T, CTRL] =
      stream.connect(supportStream.broadcast)

    /** It evaluates the `DataStream` against the model pointed out by
      * [[io.radicalbit.flink.pmml.scala.api.reader.ModelReader]]; it takes as input an UDF `(T, PmmlModel) => R)` .
      * It's modeled on top of `EvaluationFunction`.
      *
      * @param modelReader the [[io.radicalbit.flink.pmml.scala.api.reader.ModelReader]] instance
      * @param f UDF function
      * @tparam R The output type
      * @return `R`
      */
    def evaluate[R: TypeInformation](modelReader: ModelReader)(f: (T, PmmlModel) => R): DataStream[R] = {
      val abstractOperator = new EvaluationFunction[T, R](modelReader) {
        override def flatMap(value: T, out: Collector[R]): Unit = out.collect(f(value, evaluator))
      }

      stream.flatMap(abstractOperator)
    }

  }

  /**
    * It wraps the connected `<event,model>` stream and provides the evaluate function.
    *
    * @param connectedStream the connected stream: it chains the event Stream and the models control Stream
    * @tparam T Type information relative to the main event stream
    */
  implicit class RichConnectedStream[T <: BaseEvent: TypeInformation: ClassTag, CTRL <: ServingMessage](
      connectedStream: ConnectedStreams[T, CTRL]) {

    /**
      * It provides the evaluation function by applying
      * [[io.radicalbit.flink.pmml.scala.api.functions.EvaluationCoFunction]] to the connected streams.
      *
      * The first flatMap handles the event stream and applies the UDF (i.e. executing the punctual prediction)
      * The second flatMap handles models control stream and records the information relative to current model
      * and update the model instance
      *
      * @param f UDF for prediction manipulation and pre/post-processing logic
      * @tparam R UDF return type
      * @return The prediction output as defined by the UDF
      */
    def evaluate[R: TypeInformation](f: (T, PmmlModel) => R): DataStream[R] = {

      val abstractOperator = new EvaluationCoFunction[T, CTRL, R] {

        override def processElement1(event: T, ctx: CoProcessFunction[T, CTRL, R]#Context, out: Collector[R]): Unit = {
          val model = servingModels.getOrElse(event.modelId.hashCode, fromMetadata(event.modelId))
          out.collect(f(event, model))
        }

      }

      connectedStream.process(abstractOperator)
    }

  }

  /** Enriches Flink DataStream with [[evaluate]] on
    * [[https://ci.apache.org/projects/flink/flink-docs-release-1.2/dev/libs/ml/index.html FlinkML]]
    * [[org.apache.flink.ml.math.Vector]] input stream
    *
    * @param stream The input stream
    * @tparam V The input stream inner type; it is subclass of [[org.apache.flink.ml.math.Vector]]
    */
  implicit class QuickDataStream[V <: Vector: TypeInformation: ClassTag](stream: DataStream[V]) {

    /** Evaluates the `DataStream` against PmmlModel by invoking [[RichDataStream]] `evaluate` method.
      * It returns directly the prediction along with the input vector.
      *
      * @param modelReader The reader instance coupled to model source path.
      * @return (Prediction, V)
      */
    def evaluate(modelReader: ModelReader): DataStream[(Prediction, V)] =
      new RichDataStream[V](stream).evaluate(modelReader) { (vec, model) =>
        val result: Prediction = model.predict(vec, None)
        (result, vec)
      }
  }

}
