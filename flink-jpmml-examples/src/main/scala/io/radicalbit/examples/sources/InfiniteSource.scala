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

import java.util.concurrent.atomic.AtomicBoolean

import io.radicalbit.flink.pmml.scala.models.control.{AddMessage, ServingMessage}
import org.apache.flink.streaming.api.functions.source.SourceFunction

import scala.util.Random

/**
  * Infinite Control Source
  * @param mappingIdPath The list of id, models paths mapping
  * @param policy The generation policy
  */
class InfiniteSource(mappingIdPath: Map[String, String], policy: ControlSource.Mode, maxInterval: Long)
    extends SourceFunction[ServingMessage] {

  private val isRunning: AtomicBoolean = new AtomicBoolean(true)

  private val rand: Random = scala.util.Random

  override def cancel(): Unit = isRunning.set(false)

  /**
    * Since it's unbounded source, he generates events as long as the job lives, abiding by user defined policy
    * @param ctx The Flink SourceContext
    */
  override def run(ctx: SourceFunction.SourceContext[ServingMessage]): Unit =
    if (policy == ControlSource.Loop) loopedGeneration(ctx) else randomGeneration(ctx)

  private def loopedGeneration(context: SourceFunction.SourceContext[ServingMessage]) =
    while (isRunning.get()) {
      mappingIdPath.foreach { tuple =>
        val (id, path) = tuple
        context.getCheckpointLock.synchronized {
          context.collect(AddMessage(id, 1, path, Utils.now))
        }

        Thread.sleep(rand.nextDouble() * maxInterval toLong)
      }
    }

  private def randomGeneration(context: SourceFunction.SourceContext[ServingMessage]) =
    while (isRunning.get()) {
      val (currentId, currentPath) = rand.shuffle(mappingIdPath).head
      context.getCheckpointLock.synchronized {
        context.collect(AddMessage(currentId, 1, currentPath, Utils.now))
      }

      Thread.sleep(rand.nextDouble() * maxInterval toLong)
    }

}
