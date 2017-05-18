package io.radicalbit.flink.pmml.scala

import org.dmg.pmml.Model
import org.jpmml.evaluator.ModelEvaluator

package object api {
  type Evaluator = ModelEvaluator[_ <: Model]
  type PmmlInput = Map[String, Any]

  class InputValidationException(msg: String) extends Exception(msg)

  class InputPreparationException(msg: String) extends Exception(msg)

  class JPMMLExtractionException(msg: String) extends Exception(msg)

  class ModelLoadingException(msg: String, throwable: Throwable) extends RuntimeException(msg, throwable)

}
