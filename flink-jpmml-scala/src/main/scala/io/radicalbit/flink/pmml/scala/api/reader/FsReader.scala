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

package io.radicalbit.flink.pmml.scala.api.reader

import java.io.Closeable

import org.apache.flink.core.fs.Path

import scala.util.control.Exception.allCatch

/** Self type trait extending [[ModelReader]] features by providing automatic
  * path building, allowing to load models from any Flink supported distributed backend
  *
  */
private[api] trait FsReader { self: ModelReader =>

  private def closable[T <: Closeable, R](t: T)(f: T => R): R =
    allCatch.andFinally(t.close()).apply(f(t))

  /** Loan pattern ensuring the resource is loaded once and then closed.
    *
    * @return
    */
  private[api] def buildDistributedPath: String = {
    val pathFs = new Path(self.sourcePath)
    val fs = pathFs.getFileSystem

    closable(fs.open(pathFs)) { is =>
      scala.io.Source.fromInputStream(is).mkString
    }

  }
}
