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

package io.radicalbit.flink.pmml.scala.sources

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.util.FlinkRuntimeException

/**
  * This class can be used when you need to test connect operator in Flink
  * It streams a sequence of item according to the order of the inputList
  * interval defines the time gap between streaming two items
  *
  * @param events Zipped sequence where the first element describes the order of sourcing and
  *               the second is the event to be sourced
  * @tparam L The type of the first event which needs to be sourced
  * @tparam R The type of the second event which needs to be sourced
  */
class TemporizedSourceFunction[L, R](events: Seq[(Option[L], Option[R])]) extends SourceFunction[Either[L, R]] {

  override def run(ctx: SourceFunction.SourceContext[Either[L, R]]): Unit = {

    events.foreach { tuple: (Option[L], Option[R]) =>
      val outputEvent: Either[L, R] = tuple match {
        case (Some(left), None) => Left(left)
        case (None, Some(right)) => Right(right)
        case _ => throw new FlinkRuntimeException(s"Temporized source function was not able to decode input.")
      }
      Thread.sleep(500l)
      ctx.getCheckpointLock.synchronized {
        ctx.collect(outputEvent)
      }

    }

  }

  override def cancel(): Unit = {}

}
