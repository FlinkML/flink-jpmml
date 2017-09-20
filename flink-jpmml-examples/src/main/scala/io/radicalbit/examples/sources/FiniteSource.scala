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

package io.radicalbit.examples.sources

import io.radicalbit.flink.pmml.scala.models.control.{AddMessage, ServingMessage}
import org.apache.flink.streaming.api.functions.source.SourceFunction

import scala.util.Random

/**
  * Finite Control Messages Sources
  * @param mappingIdPath The Id, models path
  * @param maxInterval The Max interval of generation between events
  */
class FiniteSource(mappingIdPath: Map[String, String], maxInterval: Long) extends SourceFunction[ServingMessage] {

  private val rand: Random = scala.util.Random

  override def cancel(): Unit = {}

  override def run(ctx: SourceFunction.SourceContext[ServingMessage]): Unit =
    mappingIdPath.foreach { idPath =>
      val (id, path) = idPath
      ctx.getCheckpointLock.synchronized {
        ctx.collect(AddMessage(id, 1, path, Utils.now))
      }

      Thread.sleep(rand.nextDouble() * maxInterval toLong)
    }

}
