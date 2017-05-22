package io.radicalbit.examples.util

import org.apache.flink.api.java.utils.ParameterTool

object EnsureParameters {
  def ensureParams(params: ParameterTool): String = {
    if (params.has("model")) params.get("model")
    else throw new IllegalArgumentException("model are required")
  }
}
