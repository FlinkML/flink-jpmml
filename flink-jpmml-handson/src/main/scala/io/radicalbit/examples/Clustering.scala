package io.radicalbit.examples

import org.apache.flink.streaming.api.scala._
import io.radicalbit.examples.model.IrisSource._
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala._
import org.apache.flink.api.java.utils.ParameterTool

object Clustering {

  def main(args: Array[String]): Unit = {
    val params: ParameterTool = ParameterTool.fromArgs(args)
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.getConfig.setGlobalJobParameters(params)
    val inputModel = if (params.has("model")) params.get("model") else throw new IllegalArgumentException("model are required")

    //Read data from custom iris source
    val irisDataStream = irisSource(env)

    //Load model
    val modelReader = ModelReader(inputModel)

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
