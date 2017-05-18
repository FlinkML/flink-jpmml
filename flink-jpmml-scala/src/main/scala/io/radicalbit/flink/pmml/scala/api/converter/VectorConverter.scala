package io.radicalbit.flink.pmml.scala.api.converter

import io.radicalbit.flink.pmml.scala.api.{Evaluator, PmmlInput}
import org.apache.flink.ml.math.{DenseVector, SparseVector, Vector}
import org.dmg.pmml.Model
import org.jpmml.evaluator.ModelEvaluator

import scala.collection.JavaConversions._

/**
  * Create A TypeClass Pattern to convert input data from flink, in Map to pass a JPMML
  */
trait VectorConverter[T] extends Serializable {
  def serializeVector(v: T, eval: Evaluator): Map[String, Any]
}

object VectorConverter {

  implicit object VectorToMapbleJpmml extends VectorConverter[Vector] {
    def serializeVector(v: Vector, eval: Evaluator): PmmlInput = {
      v match {
        case denseVector: DenseVector => DenseVectorMapbleJpmml.serializeVector(denseVector, eval)
        case sparseVector: SparseVector => SparseVectorMapbleJpmml.serializeVector(sparseVector, eval)
      }
    }
  }

  implicit object DenseVectorMapbleJpmml extends VectorConverter[DenseVector] {
    def serializeVector(v: DenseVector, eval: Evaluator): PmmlInput = {
      val getNameInput = getKeyFromModel(eval)

      getNameInput.zip(v.data).toMap
    }
  }

  implicit object SparseVectorMapbleJpmml extends VectorConverter[SparseVector] {
    def serializeVector(v: SparseVector, eval: Evaluator): PmmlInput = {
      val getNameInput = getKeyFromModel(eval)

      getNameInput.zip(toDenseData(v)).collect { case (key, Some(value)) => (key, value) }.toMap
    }

    private def toDenseData(sparseVector: SparseVector): Seq[Option[Any]] = {
      (0 to sparseVector.size).map { index =>
        if (sparseVector.indices.contains(index)) Some(sparseVector(index)) else None
      }
    }
  }

  implicit def portingToFlinkJpmml[T: VectorConverter, E <: Evaluator](dataVector: T, eval: E) =
    implicitly[VectorConverter[T]].serializeVector(dataVector, eval)

  /**
    * Used to extract all key from input
    *
    * @param evaluator
    * @return Seq[String]
    *
    */
  def getKeyFromModel(evaluator: Evaluator) =
    evaluator.getActiveFields.map(_.getName.getValue)

}