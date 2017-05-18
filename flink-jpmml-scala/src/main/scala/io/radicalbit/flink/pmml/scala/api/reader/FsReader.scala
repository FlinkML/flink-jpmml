package io.radicalbit.flink.pmml.scala.api.reader

import java.io.Closeable

import org.apache.flink.core.fs.Path

import scala.util.control.Exception.allCatch

trait FsReader { self: ModelReader =>

  def closable[T <: Closeable, R](t: T)(f: T => R): R =
    allCatch.andFinally(t.close()).apply(f(t))

  def buildDistributedPath: String = {
    val pathFs = new Path(self.sourcePath)
    val fs = pathFs.getFileSystem

    closable(fs.open(pathFs)) { is =>
      scala.io.Source.fromInputStream(is).mkString
    }

  }
}
