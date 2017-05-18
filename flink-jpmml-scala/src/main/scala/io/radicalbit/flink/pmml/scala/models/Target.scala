package io.radicalbit.flink.pmml.scala.models

object Target {
  def apply(v: Double): Target = Score(v)
  def empty = EmptyScore
}

sealed trait Target {
  final def getOrElse(default: => Double): Double =
    this match {
      case Score(v) => v
      case EmptyScore => default
    }

  def get: Double
}

final case class Score(value: Double) extends Target {
  def get: Double = value
}
case object EmptyScore extends Target {
  def get: Double = throw new NoSuchElementException("EmptyScore.nan")
}
