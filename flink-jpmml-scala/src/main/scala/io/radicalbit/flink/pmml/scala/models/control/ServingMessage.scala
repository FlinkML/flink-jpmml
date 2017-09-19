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

package io.radicalbit.flink.pmml.scala.models.control

import io.radicalbit.flink.pmml.scala.models.core.{ModelId, ModelInfo}

/** Defines the mandatory fields that events control stream must implement.
  *
  */
sealed trait ServingMessage {

  def name: String

  def occurredOn: Long

}

/** Defines a event control message in order to add a new model
  *
  * @param name of the model
  * @param version of the model
  * @param path of the model
  * @param occurredOn represents when the event occurred
  */
final case class AddMessage(name: String, version: Long, path: String, occurredOn: Long) extends ServingMessage {

  def modelId: ModelId = ModelId(name, version)

  def modelInfo: ModelInfo = ModelInfo(path)

}

/** Defines a event control message in order to delete a model
  *
  * @param name of the model
  * @param version of the model
  * @param occurredOn represents when the event occurred
  */
final case class DelMessage(name: String, version: Long, occurredOn: Long) extends ServingMessage {

  def modelId: ModelId = ModelId(name, version)

}
