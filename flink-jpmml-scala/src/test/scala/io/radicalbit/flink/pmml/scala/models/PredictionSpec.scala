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
