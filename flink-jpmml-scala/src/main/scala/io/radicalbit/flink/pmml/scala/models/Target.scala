/*
 * flink-jpmml
 * Copyright (C) 2017 Radicalbit

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
