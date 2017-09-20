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
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.models.control.DelMessage
import io.radicalbit.flink.pmml.scala.models.core.ModelId
import io.radicalbit.flink.pmml.scala.utils.PmmlLoaderKit
import org.scalatest.{Matchers, WordSpec}

import scala.collection.{immutable, mutable}

abstract class ModelsManagerSpec[M: ModelsManager] extends WordSpec with Matchers with PmmlLoaderKit {

  val modelName = "model"
  val modelVersion = 1
  val modelPath: String = getPMMLSource(Source.KmeansPmml)

  val modelId: Int = ModelId(modelName, modelVersion).hashCode

  private val pmmlModel = PmmlModel.fromReader(ModelReader(getPMMLSource(Source.KmeansPmml42)))
  private val unknownIn = mutable.Map(scala.util.Random.nextInt() -> pmmlModel)
  private val in = mutable.Map(modelId -> pmmlModel)

  def controlMessage: M

  def onOut(in: mutable.Map[Int, PmmlModel]): immutable.Set[Int]

  def knownOut: immutable.Set[Int] = onOut(in)
  def unknownOut: immutable.Set[Int] = onOut(unknownIn)

  "ModelsManager" should {

    "remove models method correctly on known models identifier" in {
      ModelsManager(controlMessage, in) shouldBe knownOut
    }

    "remove nothing on unknown models identifier" in {
      ModelsManager(controlMessage, unknownIn) shouldBe unknownOut
    }

  }

}

class ModelsManagerDelMessageSpec extends ModelsManagerSpec[DelMessage] {

  override lazy val controlMessage: DelMessage = DelMessage(modelName, modelVersion, System.currentTimeMillis())

  override def onOut(in: mutable.Map[Int, PmmlModel]): Set[Int] =
    in.keySet.intersect(Set(controlMessage.modelId.hashCode)).toSet

}
