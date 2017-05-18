package io.radicalbit.flink.pmml.scala.api.functions

import io.radicalbit.flink.pmml.scala.ModelLoadingException
import io.radicalbit.flink.pmml.scala.api.PmmlModel
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.logging.LazyLogging
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.configuration.Configuration

import scala.util.{Failure, Success, Try}

private[scala] abstract class EvaluationFunction[IN, OUT](reader: ModelReader) extends RichFlatMapFunction[IN, OUT] with LazyLogging {

  protected lazy val evaluator: PmmlModel = PmmlModel.fromReader(reader)

  override def open(parameters: Configuration): Unit = {
    Try(evaluator.evaluator.getModel) match {
      case Success(model) => logger.info(s"Model has been read successfully, model name: ${model.getModelName}")
      case Failure(e) => throw new ModelLoadingException(e.getMessage, e)
    }
  }

}
