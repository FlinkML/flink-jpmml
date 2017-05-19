package io.radicalbit.examples

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import io.radicalbit.examples.model.IrisSource._

object Clustering {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    //Read model from custom iris source
    val irisDataStream = irisSource(env)

    irisDataStream.print()
    env.execute("Clustering example")
  }
}
