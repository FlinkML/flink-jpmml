package io.radicalbit.flink.pmml.scala

import org.dmg.pmml.Model
import org.jpmml.evaluator.ModelEvaluator

package object api {

  type Evaluator = ModelEvaluator[_ <: Model]
  type PmmlInput = Map[String, Any]

}
