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

import io.radicalbit.flink.pmml.scala.{InputPreparationException, InputValidationException, JPMMLExtractionException}
import org.jpmml.evaluator.EvaluationException
import org.scalatest.{Matchers, WordSpec}

import scala.util.Try

class PredictionSpec extends WordSpec with Matchers {

  private def throwableFunc[E <: Exception](exception: E)(f: PartialFunction[Throwable, Prediction]) =
    Try(throw exception).recover(f).get

  "Prediction" should {

    "extract prediction if the extraction is Success" in {
      Prediction.extractPrediction(Try(2.0)) shouldBe Prediction(Score(2.0))
    }

    "testing prediction getOrElse EmptyScore" in {
      val emptyPrediction: Prediction = Prediction(Target.empty)
      emptyPrediction.value.getOrElse(-1.0) shouldBe -1.0
    }

    "tesing prediction getOrElse with Score" in {
      val prediction: Prediction = Prediction(Score(3.0))
      prediction.value.getOrElse(-1.0) shouldBe 3.0
    }

    "extract empty prediction if the extraction is Failure" in {
      Prediction.extractPrediction(Try(2 / 0)) shouldBe Prediction(EmptyScore)
    }

    "return None if onFailedPrediction is active and JPMMLExtractionException" in {
      throwableFunc(new JPMMLExtractionException("")) {
        case e: Throwable => Prediction.onFailedPrediction(e)
      } shouldBe Prediction(EmptyScore)
    }

    "return None if onFailedPrediction is active and InputPreparationException" in {
      throwableFunc(new InputPreparationException("")) {
        case e: Throwable => Prediction.onFailedPrediction(e)
      } shouldBe Prediction(EmptyScore)
    }

    "return None if onFailedPrediction is active and InputValidationException" in {
      throwableFunc(new InputValidationException("")) {
        case e: Throwable => Prediction.onFailedPrediction(e)
      } shouldBe Prediction(EmptyScore)
    }

    "return None if onFailedPrediction is active and EvaluationException" in {
      throwableFunc(new EvaluationException) {
        case e: Throwable => Prediction.onFailedPrediction(e)
      } shouldBe Prediction(EmptyScore)
    }

    "return None if onFailedPrediction is active and ClassCastException" in {
      throwableFunc(new ClassCastException) {
        case e: Throwable => Prediction.onFailedPrediction(e)
      } shouldBe Prediction(EmptyScore)
    }

    "return None if onFailedPrediction is active and whatever Exception" in {
      throwableFunc(new Exception) {
        case e: Throwable => Prediction.onFailedPrediction(e)
      } shouldBe Prediction(EmptyScore)
    }

  }

}
