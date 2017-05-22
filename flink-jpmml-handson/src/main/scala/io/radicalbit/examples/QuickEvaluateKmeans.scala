package io.radicalbit.examples

import org.apache.flink.streaming.api.scala._
import io.radicalbit.examples.model.IrisSource._
import io.radicalbit.examples.util.EnsureParameters
import io.radicalbit.flink.pmml.scala._
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import org.apache.flink.api.java.utils.ParameterTool


object QuickEvaluateKmeans extends EnsureParameters{
  def main(args: Array[String]): Unit = {
    val params: ParameterTool = ParameterTool.fromArgs(args)
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.getConfig.setGlobalJobParameters(params)
    val (inputModel, output) = ensureParams(params)

    //Read data from custom iris source
    val irisDatastream = irisSource(env)

    //Convert iris to DenseVector
    val irisToVector = irisDatastream.map(_.toVector)

    //Load PMML model
    val model = ModelReader(inputModel)

    //Quick evaluate
    irisToVector.evaluate(model).writeAsText(output)

    env.execute("Quick evaluator Clustering")
  }
}
