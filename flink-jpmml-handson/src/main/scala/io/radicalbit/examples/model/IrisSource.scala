package io.radicalbit.examples.model
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._

object IrisSource {
  private final val numberOfParameters = 4
  private final lazy val randomGenerator = scala.util.Random
  private final val RANDOM_MIN = 0.2
  private final val RANDOM_MAX = 6.0

  private final def truncateDouble(n: Double) = (math floor n * 10) / 10

  @throws(classOf[Exception])
  def irisSource(env: StreamExecutionEnvironment): DataStream[Iris] = {

    env.addSource((sc: SourceContext[Iris]) => {
      while (true) {
        def randomVal = RANDOM_MIN + (RANDOM_MAX - RANDOM_MIN) * randomGenerator.nextDouble()
        val dataForIris = Seq.fill(numberOfParameters)(truncateDouble(randomVal))
        val iris = Iris(dataForIris(0), dataForIris(1), dataForIris(2), dataForIris(3))
        sc.collect(iris)
        Thread.sleep(1000)
      }
    })

  }

}
