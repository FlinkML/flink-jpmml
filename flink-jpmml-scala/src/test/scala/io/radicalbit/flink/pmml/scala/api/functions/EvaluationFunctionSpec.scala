package io.radicalbit.flink.pmml.scala.api.functions

import io.radicalbit.flink.pmml.scala.RichDataStreamSpec.Input
import io.radicalbit.flink.pmml.scala.api.PmmlModel
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.models.{Prediction, Score}
import io.radicalbit.flink.pmml.scala.utils.{FlinkPipelineTestKit, FlinkTestKitCompanion, PmmlLoaderKit}
import org.apache.flink.api.scala.ClosureCleaner
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object EvaluationFunctionSpec extends FlinkTestKitCompanion[Prediction]

class EvaluationFunctionSpec extends FlinkPipelineTestKit[Input, Prediction] with PmmlLoaderKit {

  private val reader = ModelReader(getPMMLSource(Source.KmeansPmml))

  private def evaluationOperator[T](source: ModelReader)(f: (T, PmmlModel) => Prediction) =
    new EvaluationFunction[T, Prediction](source) {
      override def flatMap(value: T, out: Collector[Prediction]): Unit = out.collect(f(value, evaluator))
    }

  private val operator = evaluationOperator(reader) { (in: Input, model: PmmlModel) =>
    Prediction(Score(1.0))
  }

  private def pipeline(source: DataStream[Input]): DataStream[Prediction] = source.flatMap(operator)

  "EvaluationFunction" should {

    "be Serializable" in {
      noException should be thrownBy ClosureCleaner.clean(operator, checkSerializable = true)
    }

    "return expected behavior on given function" in {
      run(Seq(Input(1.0, 2.0)), Seq(Prediction(Score(1.0))), EvaluationFunctionSpec)(pipeline)
    }

  }

}
