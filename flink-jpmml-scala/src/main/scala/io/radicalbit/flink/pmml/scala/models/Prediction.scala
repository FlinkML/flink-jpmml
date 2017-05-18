package io.radicalbit.flink.pmml.scala.models

import com.typesafe.scalalogging.LazyLogging
import io.radicalbit.flink.pmml.scala.{InputPreparationException, InputValidationException, JPMMLExtractionException}
import org.jpmml.evaluator.EvaluationException

import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

object Prediction extends LazyLogging {

  def extractPrediction(out: Try[Double]): Prediction = out match {
    case Success(result) => Prediction(Score(result))
    case Failure(throwable) => onFailedPrediction(throwable)
  }

  def onFailedPrediction(throwable: Throwable): Prediction = {
    throwable match {
      case e: JPMMLExtractionException => logger.warn("Error while extracting results. The cause is: {}", e.getMessage)
      case e: InputPreparationException => logger.warn("Error while preparing input. The cause is: {}", e.getMessage)
      case e: InputValidationException => logger.warn("Error while validate input. The cause is: {}", e.getMessage)
      case e: EvaluationException => logger.warn("Error while evaluate model. The cause is: {}", e.getMessage)
      case e: ClassCastException => logger.error("Error while extract target. The cause is: {}", e.getMessage)
      case NonFatal(e) => logger.error("Error. The cause is: {}\n", e.getMessage)
    }
    emptyTarget
  }
  private def emptyTarget = Prediction(EmptyScore)
}

case class Prediction(value: Target)
