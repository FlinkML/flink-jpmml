/*
 * Copyright (C) 2017  Radicalbit
 *
 * This file is part of flink-JPMML
 *
 * flink-JPMML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * flink-JPMML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with flink-JPMML.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.radicalbit.flink.pmml.scala.api

package object exceptions {

  /** Models conformity failure between PMML model and input [[org.apache.flink.streaming.api.scala.DataStream]]
    *
    */
  private[scala] class InputValidationException(msg: String) extends RuntimeException(msg)

  /** Models [[org.jpmml.evaluator.EvaluatorUtil.prepare()]] method failure
    *
    */
  private[scala] class InputPreparationException(msg: String) extends RuntimeException(msg)

  /** Models empty result from [[org.jpmml.evaluator.ModelEvaluator]] evaluation
    *
    */
  private[scala] class JPMMLExtractionException(msg: String) extends RuntimeException(msg)

  /** Models failure on loading PMML model from distributed system
    *
    */
  private[scala] class ModelLoadingException(msg: String, throwable: Throwable)
      extends RuntimeException(msg, throwable)

  /** Prediction failure due to [[io.radicalbit.flink.pmml.scala.api.EmptyEvaluator]]
    *
    */
  private[scala] class EmptyEvaluatorException(msg: String) extends NoSuchElementException(msg)

  /** Parsing of ModelId has failed
    *
    */
  private[scala] class WrongModelIdFormat(msg: String) extends ArrayIndexOutOfBoundsException(msg)

}
