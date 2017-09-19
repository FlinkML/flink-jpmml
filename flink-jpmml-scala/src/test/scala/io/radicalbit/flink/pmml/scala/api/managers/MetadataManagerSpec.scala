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

import io.radicalbit.flink.pmml.scala.models.control.{AddMessage, DelMessage}
import io.radicalbit.flink.pmml.scala.models.core.{ModelId, ModelInfo}
import io.radicalbit.flink.pmml.scala.utils.PmmlLoaderKit
import org.scalatest.{Matchers, WordSpec}

import scala.collection.immutable

abstract class MetadataManagerSpec[M: MetadataManager] extends WordSpec with Matchers with PmmlLoaderKit {

  val modelName = "model"
  val modelVersion = 1
  val modelPath: String = getPMMLSource(Source.KmeansPmml)

  val modelId: ModelId = ModelId(modelName, modelVersion)
  val modelInfo = ModelInfo(modelPath)

  val in = immutable.Map(modelId -> modelInfo)
  val unknownIn = immutable.Map(ModelId("unknown-id", scala.util.Random.nextLong()) -> modelInfo)

  def outOnKnown: immutable.Map[ModelId, ModelInfo] = toOut(in)
  def outOnUnknown: immutable.Map[ModelId, ModelInfo] = toOut(unknownIn)

  def toOut(in: immutable.Map[ModelId, ModelInfo]): immutable.Map[ModelId, ModelInfo]
  def controlMessage: M

  "MetadataManager" should {

    "manage metadata correctly if targeted model is not already in metadata (Add add metadata, Del returns input)" in {
      MetadataManager(controlMessage, unknownIn) shouldBe outOnUnknown
    }

    "manage metadata correctly if targeted model already exists (Add returns input, Del removes metadata)" in {
      MetadataManager(controlMessage, in) shouldBe outOnKnown
    }

  }

}

class AddMetadataManagerSpec extends MetadataManagerSpec[AddMessage] {
  override lazy val controlMessage: AddMessage =
    AddMessage(modelName, modelVersion, modelPath, System.currentTimeMillis())

  override def toOut(in: immutable.Map[ModelId, ModelInfo]): immutable.Map[ModelId, ModelInfo] =
    in.get(modelId) match {
      case Some(_) => in
      case None => in + (modelId -> modelInfo)
    }
}

class RemoveMetadataManagerSpec extends MetadataManagerSpec[DelMessage] {
  override lazy val controlMessage: DelMessage = DelMessage(modelName, modelVersion, System.currentTimeMillis())

  override def toOut(in: immutable.Map[ModelId, ModelInfo]): immutable.Map[ModelId, ModelInfo] =
    in - ModelId(modelName, modelVersion)
}
