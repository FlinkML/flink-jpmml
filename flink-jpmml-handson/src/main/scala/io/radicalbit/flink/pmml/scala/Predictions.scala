package io.radicalbit.flink.pmml.scala

import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.ml.math.{DenseVector, SparseVector, Vector}
import org.apache.flink.streaming.api.scala._
import io.radicalbit.flink.pmml.scala.models.Prediction

import io.radicalbit.flink.pmml.scala.api._

import scala.util.Try

class NormalizedCBData extends MapFunction[String, Vector] {
  def map(record: String): DenseVector = {
    val splitInputAndReturnTail = record split "," tail
    val convertArrayOfDoube = splitInputAndReturnTail map { x =>
      Try(x.toDouble).getOrElse(0.0)
    }
    DenseVector(convertArrayOfDoube)
  }
}

class CBDataToSparseVector extends MapFunction[String, (SparseVector, Map[String, String])] {
  def map(record: String) = {
    val splitted = record split ","

    (SparseVector(4, splitted.zipWithIndex.collect {
       case (value, index) if value != "" => index
     }, splitted.zipWithIndex.collect { case (value, index) if value != "" => value.toDouble }),
     Map("ip" -> "127.0.0.1"))
  }
}

object Predictions {
  def main(args: Array[String]): Unit = {

    val params: ParameterTool = ParameterTool.fromArgs(args)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(2)

    env.getConfig.setGlobalJobParameters(params)
    val (inputPath, modelPath, outputPath) = (params.has("input"), params.has("model"), params.has("output")) match {
      case (true, true, true) => (params.get("input"), params.get("model"), params.get("output"))
      case _ => throw new IllegalArgumentException("FILE INPUT/MODEL/OUTPUT ARE REQUIRED")
    }

    //Read data and clean it
    val dataStream = env.readTextFile(inputPath)
    val cleanAndVector = dataStream.map(new CBDataToSparseVector)
    cleanAndVector.print()

    //Create a predictor object and execute prediction
    val predictor = ModelReader(modelPath)

    val outStream = cleanAndVector.evaluate(predictor) { (event, model) =>
      val vectorized = event._1
      val predictions: Prediction = model.predict(vectorized, Some(1.0))
      (predictions.value.getOrElse(-1.0), event)
    }

    env.execute("CV PREDICTOR")
  }
}
