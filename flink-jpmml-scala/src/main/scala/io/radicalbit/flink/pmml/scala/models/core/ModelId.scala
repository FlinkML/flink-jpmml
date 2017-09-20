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

package io.radicalbit.flink.pmml.scala.models.core

import io.radicalbit.flink.pmml.scala.api.exceptions.WrongModelIdFormat

import scala.util.parsing.combinator.RegexParsers

/** Provides the regular expressions for name, version and id of a model;
  * Name is a UUID
  * Version is a Long
  * the id of a model is modelled by the following pattern: name + separatorSymbol(`_`) + version
  *
  */
object ModelId extends RegexParsers {

  final val separatorSymbol = "_"

  lazy val name = """[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}""".r ^^ { _.toString }
  lazy val version = """^\d+$""".r ^^ { _.toLong }
  lazy val nameAndVersion = name ~ separatorSymbol ~ version ^^ {
    case name ~ _ ~ version => ModelId(name, version)
  }

  /** Parses and validates the model id
    *
    * @param id name and version of the model as id
    * @return [[ModelId]] according to the provided id
    */
  def fromIdentifier(id: String): ModelId =
    parse(nameAndVersion, id) match {
      case Success(checked, _) => checked
      case NoSuccess(msg, _) => throw new WrongModelIdFormat(msg)
    }

}

/** Represents an instance of the [[ModelId]]
  *
  * @param name Name identifying the model
  * @param version The version of the model
  */
final case class ModelId(name: String, version: Long) {

  override def hashCode: Int = (name + ModelId.separatorSymbol + version).hashCode

}
