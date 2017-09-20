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
import io.radicalbit.flink.pmml.scala.utils.{PmmlEvaluatorKit, PmmlLoaderKit}
import org.jpmml.evaluator.ModelEvaluatorFactory
import org.scalatest.{Matchers, WordSpec}

class EvaluatorSpec extends WordSpec with Matchers with PmmlEvaluatorKit with PmmlLoaderKit {

  private val pmmlModel = ModelEvaluatorFactory.newInstance.newModelEvaluator(getPMMLResource(Source.KmeansPmml))
  private val pmmlEvaluator = Evaluator(pmmlModel)

  private val emptyEvaluator =
    Evaluator.empty

  "Evaluator" should {

    "have right box called PmmlEvaluator and with right model" in {
      pmmlEvaluator shouldBe PmmlEvaluator(pmmlModel)
    }

    "have right empty box called EmptyEvaluator and with right value" in {
      emptyEvaluator shouldBe EmptyEvaluator
    }

    "have a model method that return the pmml model" in {
      pmmlEvaluator.model shouldBe pmmlModel
    }

    "have a getOrElse method that return pmml model" in {
      emptyEvaluator.getOrElse(pmmlModel) shouldBe pmmlModel
      pmmlEvaluator.getOrElse(pmmlModel) shouldBe pmmlModel
    }

    "throw an EmptyEvaluatorException if call model on emptyEvaluator" in {
      an[EmptyEvaluatorException] should be thrownBy emptyEvaluator.model
    }
  }
}
