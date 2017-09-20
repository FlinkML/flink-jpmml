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

import io.radicalbit.flink.pmml.scala.api.exceptions.EmptyEvaluatorException
import org.dmg.pmml.Model
import org.jpmml.evaluator.ModelEvaluator

/**
  * Represents the Evaluator of a PmmlModel
  */
object Evaluator {

  /** If the evaluator exists, it returns a [[PmmlEvaluator]], [[EmptyEvaluator]] otherwise.
    *
    * @param evaluator An instance of [[ModelEvaluator]]
    * @return An instance of [[Evaluator]]
    */
  def apply(evaluator: ModelEvaluator[_ <: Model]): Evaluator = PmmlEvaluator(evaluator)

  /** Returns an empty instance of [[Evaluator]]
    *
    * @return An [[EmptyEvaluator]]
    */
  def empty: Evaluator = EmptyEvaluator

}

/**
  * ADT sealed trait providing getters for ModelEvaluator
  */
sealed trait Evaluator {

  def model: ModelEvaluator[_ <: Model]

  /** Returns [[ModelEvaluator]]] if evaluator has value, default value otherwise
    *
    * @param default the defined default value
    * @return the current evaluator if it has value, default otherwise
    */
  def getOrElse(default: => ModelEvaluator[_ <: Model]) =
    this match {
      case PmmlEvaluator(evaluator) => evaluator
      case EmptyEvaluator => default
    }

}

/**
  * Represents the Evaluator if it is not present
  */
case object EmptyEvaluator extends Evaluator {

  /** Implements model method when the [[PmmlEvaluator]] is empty
    *
    * @return scala.NoSuchElementException for `EmptyEvaluator` instance.
    */
  override def model: ModelEvaluator[_ <: Model] = throw new EmptyEvaluatorException("EmptyEvaluator.None")

}

/**
  * Represents the Evaluator if it is present
  * @param modelEval the evaluator for the Pmml Model
  */
final case class PmmlEvaluator(modelEval: ModelEvaluator[_ <: Model]) extends Evaluator {

  /**
    * Retrieving the evaluator of the JpmmlEvaluator
    * @return the [[ModelEvaluator]]
    */
  override def model: ModelEvaluator[_ <: Model] = modelEval

}
