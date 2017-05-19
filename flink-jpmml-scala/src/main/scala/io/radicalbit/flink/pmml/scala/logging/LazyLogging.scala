package io.radicalbit.flink.pmml.scala.logging

import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory

trait LazyLogging {
  protected lazy val logger = {
    Logger(LoggerFactory.getLogger(getClass.getName))
  }
}
