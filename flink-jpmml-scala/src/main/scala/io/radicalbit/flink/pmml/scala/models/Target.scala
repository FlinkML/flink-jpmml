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

/** Represents the result output value; if the target is present has [[Score]] value,
  * [[EmptyScore]] otherwise.
  */
object Target {

  /** Factory method
    *
    * @param v
    * @return
    */
  def apply(v: Double): Target = Score(v)

  /** Returns an empty instance of [[Target]]
    *
    * @return
    */
  def empty = EmptyScore
}

/** ADT sealed trait providing getters for values.
  *
  */
sealed trait Target {

  /** Returns [[Score]]] if target has value, default value otherwise
    *
    * @param default the user defined default value
    * @return
    */
  final def getOrElse(default: => Double): Double =
    this match {
      case Score(v) => v
      case EmptyScore => default
    }

  def get: Double
}

/** Represents the Target value if it is present
  *
  * @param value the prediction value as [[scala.Double]]
  */
final case class Score(value: Double) extends Target {

  /** Returns the [[Score]] value
    *
    * @return
    */
  def get: Double = value
}

/** Represents the Target value if it is not present
  *
  */
case object EmptyScore extends Target {

  /** Implements get method when the [[Score]] is empty
    *
    * @return
    * @throws scala.NoSuchElementException for `EmptyScore` instance.
    */
  def get: Double = throw new NoSuchElementException("EmptyScore.nan")
}
