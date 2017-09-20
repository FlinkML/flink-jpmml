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

package io.radicalbit.flink.pmml.scala.models.core

import io.radicalbit.flink.pmml.scala.api.exceptions.WrongModelIdFormat
import org.scalatest.{Matchers, WordSpec}

class ModelIdSpec extends WordSpec with Matchers {
  private val uuid = "2bc91c20-bf64-4860-ba11-de97a95d05cf"
  private val version = "23"

  "Parser" should {

    "matched uuid" in {
      val modelId = uuid + ModelId.separatorSymbol + version
      val modelIdExpected = ModelId(uuid, 23)

      ModelId.fromIdentifier(modelId) shouldBe modelIdExpected
    }

    "wrong uuid" in {
      val wrongUuid = "23441-" + ModelId.separatorSymbol + version
      an[WrongModelIdFormat] should be thrownBy ModelId.fromIdentifier(wrongUuid)
    }

  }

  "ModelId" should {

    "return the right hash code" in {
      val fullId = uuid + ModelId.separatorSymbol + version
      val modelId = ModelId(uuid, 23)

      modelId.hashCode shouldBe fullId.hashCode
    }
  }

}
