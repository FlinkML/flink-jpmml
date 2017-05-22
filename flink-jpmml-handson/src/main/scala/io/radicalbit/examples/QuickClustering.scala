package io.radicalbit.examples

import org.apache.flink.streaming.api.scala._
import io.radicalbit.examples.model.IrisSource._
import io.radicalbit.flink.pmml.scala._
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader

object QuickClustering {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(2)

    val irisDatastream = irisSource(env)
    val irisToVector = irisDatastream.map(_.toVector)

    val model = ModelReader(getClass.getResource("/kmeans.xml").getPath)

    irisToVector.evaluate(model).print()

    env.execute("Quick evaluator Clustering")
  }
}
