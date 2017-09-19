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

import io.radicalbit.flink.pmml.scala.logging.LazyLogging
import io.radicalbit.flink.pmml.scala.models.control.{AddMessage, DelMessage}
import io.radicalbit.flink.pmml.scala.models.core.{ModelId, ModelInfo}

import scala.collection.immutable

/**
  * Type class in order to enrich Message Protocol ADT with proper methods for acting on metadata.
  *
  * @tparam T Specific Message ADT subType; it's contro-variant in order to accept super classes, then generics.
  */
sealed trait MetadataManager[T] {

  def manageMetadata(command: T, metadata: immutable.Map[ModelId, ModelInfo]): immutable.Map[ModelId, ModelInfo]

}

object MetadataManager extends LazyLogging {

  implicit def apply[T: MetadataManager](
      command: T,
      metadata: immutable.Map[ModelId, ModelInfo]): immutable.Map[ModelId, ModelInfo] =
    implicitly[MetadataManager[T]].manageMetadata(command, metadata)

  /**
    * Implicit value aimed to Adding model information to metadata.
    *
    * If a new model is coming (where new means a model bind to a previously unknown identifier)
    * so the model information is added to metadata.
    *
    * If a not new model is coming (where not new means the model has an already present identifier)
    * so a WARN is logged to the system; indeed, if the user wants to update a model, he should provide
    * a newer version for it.
    *
    * Add messages don't remove elements from metadata for any reason.
    *
    */
  implicit val addMetadataServing = new MetadataManager[AddMessage] {

    def manageMetadata(addMessage: AddMessage,
                       metadata: immutable.Map[ModelId, ModelInfo]): immutable.Map[ModelId, ModelInfo] = {
      if (metadata.contains(addMessage.modelId)) {
        logger.warn("ADD action on existing models is not possible (newer version needed). {} given.", addMessage)
        metadata
      } else metadata + (addMessage.modelId -> addMessage.modelInfo)
    }

  }

  /**
    * Implicit object aimed to removing model information from metadata.
    *
    * Del messages don't add elements to metadata for any reason.
    *
    * If a metadata element needs to be removed a key Set of the
    * to-be-removed elements is returned.
    *
    */
  implicit val deleteMetadataServing = new MetadataManager[DelMessage] {

    def manageMetadata(delMessage: DelMessage,
                       metadata: immutable.Map[ModelId, ModelInfo]): immutable.Map[ModelId, ModelInfo] =
      metadata - delMessage.modelId

  }

}
