package io.radicalbit.examples.model
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._

object IrisSource {
  private final val numberOfParameters = 4
  private final val randomGenerator = scala.util.Random

  @throws(classOf[Exception])
  def irisSource(env: StreamExecutionEnvironment): DataStream[Iris] = {

    env.addSource((sc: SourceContext[Iris]) => {
      while (true) {
        val dataForIris = Seq.fill(numberOfParameters)(randomGenerator.nextDouble)
        val iris = Iris(dataForIris(0), dataForIris(1), dataForIris(2), dataForIris(3))
        sc.collect(iris)
        Thread.sleep(1000)
      }
    })

  }

}
