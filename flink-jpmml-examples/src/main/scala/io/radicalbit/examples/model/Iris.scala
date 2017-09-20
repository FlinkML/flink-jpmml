package io.radicalbit.examples.models

import io.radicalbit.flink.pmml.scala.models.input.BaseEvent
import org.apache.flink.ml.math.DenseVector

case class Iris(modelId: String,
                sepalLength: Double,
                sepalWidth: Double,
                petalLength: Double,
                petalWidth: Double,
                occurredOn: Long)
  extends BaseEvent {
  def toVector = DenseVector(sepalLength, sepalWidth, petalLength, petalWidth)
}
