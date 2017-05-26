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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.radicalbit.flink.pmml.scala.models

import org.scalatest.{Matchers, WordSpec}

class TargetSpec extends WordSpec with Matchers {
  private val target = Target(3.0)
  private val emptyTarget = Target.empty

  "Target" should {

    "have right box called Score and with right value" in {
      target shouldBe Score(3.0)
    }

    "have right empty box called EmptyScore and with right value " in {
      emptyTarget shouldBe EmptyScore
    }

    "have a get method" in {
      target.get shouldBe 3.0
    }

    "throw a NoSuchElementException if get on emptyTarget" in {
      an[NoSuchElementException] should be thrownBy emptyTarget.get
    }

    "have a getOrElse method and return vale if is score" in {
      target.getOrElse(-1.0) shouldBe 3.0
    }

    "have a getOrElse method and return empty value if is empty score" in {
      emptyTarget.getOrElse(-1.0) shouldBe -1.0
    }

  }
}
