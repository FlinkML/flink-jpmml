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

}
