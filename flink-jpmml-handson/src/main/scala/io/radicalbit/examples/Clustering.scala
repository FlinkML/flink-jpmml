package io.radicalbit.examples

import org.apache.flink.streaming.api.scala._
import io.radicalbit.examples.model.IrisSource._
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala._
import io.radicalbit.flink.pmml.scala.models.{EmptyScore, Score, Target}

object IrisCategory extends Enumeration {
  type IrisCategory = Value
  val IrisSetosa, IrisVersicolour, IrisVirginica, NaN = Value
}
object Clustering {
  import IrisCategory._

  def matchingPrediction(prediction: Target): IrisCategory = {
    prediction.getOrElse(-1.0) match {
      case 1 => IrisSetosa
      case 2 => IrisVersicolour
      case 3 => IrisVirginica
      case _ => NaN
    }
  }

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(4)

    //Read model from custom iris source
    val irisDataStream = irisSource(env)

    //Load model
    val modelReader = ModelReader(getClass.getResource("/kmeans.xml").getPath)

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
