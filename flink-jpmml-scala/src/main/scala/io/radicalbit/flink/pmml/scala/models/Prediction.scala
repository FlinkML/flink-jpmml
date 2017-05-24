/*
 *
 * Copyright (c) 2017 Radicalbit
 *
 * This file is part of flink-JPMML
 *
 * flink-JPMML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * flink-JPMML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with flink-JPMML.  If not, see <http://www.gnu.org/licenses/>.
 *         
 */

package io.radicalbit.flink.pmml.scala.models

import io.radicalbit.flink.pmml.scala.{InputPreparationException, InputValidationException, JPMMLExtractionException}
import io.radicalbit.flink.pmml.scala.logging.LazyLogging
import org.jpmml.evaluator.EvaluationException

import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

/** Factory for [[Prediction]] case class instances */
object Prediction extends LazyLogging {

  /** Evaluates [[Try]] statement executed at [[io.radicalbit.flink.pmml.scala.api.PmmlModel.predict]]
    *
    * @param out containing evaluation pipeline execution
    * @return
    */
  private[scala] def extractPrediction(out: Try[Double]): Prediction = out match {
    case Success(result) => Prediction(Score(result))
    case Failure(throwable) => onFailedPrediction(throwable)
  }

  /** Pattern matches failures arisen by [[io.radicalbit.flink.pmml.scala.api.PmmlModel.predict]]
    *
    * @param throwable
    * @return
    */
  private[scala] def onFailedPrediction(throwable: Throwable): Prediction = {

    throwable match {
      case e: JPMMLExtractionException =>
        logger.warn("Error while extracting results. The cause is: {}\n", e)
      case e: InputPreparationException =>
        logger.warn("Error while preparing input. The cause is: {}\n", e)
      case e: InputValidationException => logger.warn("Error while validate input. The cause is: {}\n", e)
      case e: EvaluationException => logger.warn("Error while evaluate model. The cause is: {}\n", e)
      case e: ClassCastException => logger.error("Error while extract target. The cause is: {}\n", e)
      case NonFatal(e) => logger.error("Error. The cause is: {}\n", e)
    }

    emptyTarget

  }

  private def emptyTarget = Prediction(EmptyScore)

}

/** Models the result output container
  *
  * @param value contains the extracted prediction of [[Target]] type
  */
case class Prediction(value: Target)
