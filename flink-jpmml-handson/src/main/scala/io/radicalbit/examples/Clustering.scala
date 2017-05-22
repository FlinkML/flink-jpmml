package io.radicalbit.examples

import org.apache.flink.streaming.api.scala._
import io.radicalbit.examples.model.IrisSource._
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala._

object Clustering {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    //Read data from custom iris source
    val irisDataStream = irisSource(env)

    //Load model
    val modelReader = ModelReader("/kmeans.xml")

    //Using evaluate operator
    val prediction = irisDataStream.evaluate(modelReader) {
      case (event, model) => {
        val vectorized = event.toVector
        val prediction = model.predict(vectorized, Some(0.0))
        Map(prediction.value -> event)
      }
    }

    prediction.print()

    env.execute("Clustering example")
  }
}
