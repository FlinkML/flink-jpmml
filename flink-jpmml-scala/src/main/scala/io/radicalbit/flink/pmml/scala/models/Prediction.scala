/*
 * flink-jpmml
 * Copyright (C) 2017 Radicalbit

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.radicalbit.flink.pmml.scala.models

import io.radicalbit.flink.pmml.scala.logging.LazyLogging
import io.radicalbit.flink.pmml.scala.{InputPreparationException, InputValidationException, JPMMLExtractionException}
import org.jpmml.evaluator.EvaluationException

import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

object Prediction extends LazyLogging {

  private[scala] def extractPrediction(out: Try[Double]): Prediction = out match {
    case Success(result) => Prediction(Score(result))
    case Failure(throwable) => onFailedPrediction(throwable)
  }

  private[scala] def onFailedPrediction(throwable: Throwable): Prediction = {
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
