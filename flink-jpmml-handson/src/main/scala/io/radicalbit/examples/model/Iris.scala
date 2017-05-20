package io.radicalbit.examples.model

import org.apache.flink.ml.math.DenseVector

case class Iris(sepalLength: Double, sepalWidth: Double, petalLength: Double, petalWidth: Double) {
  def toVector = DenseVector(sepalLength, sepalWidth, petalLength, petalWidth)
}
