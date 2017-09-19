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

package io.radicalbit.flink.pmml.scala.api.managers

import io.radicalbit.flink.pmml.scala.api.PmmlModel
import io.radicalbit.flink.pmml.scala.logging.LazyLogging
import io.radicalbit.flink.pmml.scala.models.control.DelMessage

import scala.collection.{immutable, mutable}

/**
  * Type class in order to enrich Message Protocol ADT with proper methods for acting on models.
  *
  * @tparam T Specific Message ADT subType; it's contro-variant in order to accept super classes, then generics.
  */
sealed trait ModelsManager[T] {

  def manageModels(command: T, models: mutable.Map[Int, PmmlModel]): immutable.Set[Int]

}

object ModelsManager extends LazyLogging {

  def apply[T: ModelsManager](command: T, models: mutable.Map[Int, PmmlModel]): Set[Int] =
    implicitly[ModelsManager[T]].manageModels(command, models)

  /**
    * Implicit value aimed to removing models from internal operator state on DelMessage.
    *
    * [[deleteModelsServing.manageModels]] returns the elements key set which need to be
    * removed from models.
    *
    */
  implicit val deleteModelsServing = new ModelsManager[DelMessage] {

    def manageModels(delMessage: DelMessage, models: mutable.Map[Int, PmmlModel]): immutable.Set[Int] = {
      models.keySet.intersect(Set(delMessage.modelId.hashCode)).toSet
    }

  }

}
