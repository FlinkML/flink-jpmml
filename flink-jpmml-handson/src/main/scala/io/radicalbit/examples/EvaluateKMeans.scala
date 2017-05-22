package io.radicalbit.examples

import org.apache.flink.streaming.api.scala._
import io.radicalbit.examples.model.IrisSource._
import io.radicalbit.examples.util.EnsureParameters.ensureParams
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala._
import org.apache.flink.api.java.utils.ParameterTool

object EvaluateKMeans {

  def main(args: Array[String]): Unit = {
    val params: ParameterTool = ParameterTool.fromArgs(args)
    implicit val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.getConfig.setGlobalJobParameters(params)
    val inputModel = ensureParams(params)

    //Read data from custom iris source
    val irisDataStream = irisSource(env)

    //Load model
    val modelReader = ModelReader(inputModel)

    //Using evaluate operator
    val prediction = irisDataStream.evaluate(modelReader) {
      //Iris data and modelReader instance
      case (event, model) => {
        val vectorized = event.toVector
        val prediction = model.predict(vectorized, Some(0.0))
        Map(event -> prediction)
      }
    }

    prediction.print()

    env.execute("Clustering example")
  }
}
