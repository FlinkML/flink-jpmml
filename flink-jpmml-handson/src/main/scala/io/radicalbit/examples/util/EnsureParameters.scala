package io.radicalbit.examples.util

import org.apache.flink.api.java.utils.ParameterTool

object EnsureParameters {
  def ensureParams(params: ParameterTool) = {
    (params.has("model"), params.has("output")) match {
      case (true, true) => (params.get("model"), params.get("output"))
      case _ => throw new IllegalArgumentException("model are required")
    }
  }
}
