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

package io.radicalbit.flink.pmml.scala.api.functions

import io.radicalbit.flink.pmml.scala.api.PmmlModel
import io.radicalbit.flink.pmml.scala.api.exceptions.ModelLoadingException
import io.radicalbit.flink.pmml.scala.api.managers.{MetadataManager, ModelsManager}
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.logging.LazyLogging
import io.radicalbit.flink.pmml.scala.models.control.{AddMessage, DelMessage, ServingMessage}
import io.radicalbit.flink.pmml.scala.models.core.{ModelId, ModelInfo}
import io.radicalbit.flink.pmml.scala.models.state.CheckpointType.MetadataCheckpoint
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.api.common.typeinfo.{TypeHint, TypeInformation}
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.util.Collector

import scala.collection.JavaConverters._
import scala.collection.{immutable, mutable}
import scala.util.{Failure, Success, Try}

/** Abstract class extending a [[CoProcessFunction]]; it provides:
  * two maps for caching both [[PmmlModel]] and Metadata of each Model
  * the open method in order to initialize the Metadata Map
  * the processElement2 in order to handle models and metadata against a control stream
  *
  * Abstract class extends [[CheckpointedFunction]] and provides therefore:
  * the snapshotState method in order to checkpoint the current state of the operator
  * the initializeState in order to provide an initial state at the operator and/or restore the latest
  *
  * @tparam EVENT The input Type of the event to predict
  * @tparam CTRL The control stream Type. Note: It must extend [[io.radicalbit.flink.pmml.scala.models.control.ServingMessage]]
  * @tparam OUT The output Type
  */
private[scala] abstract class EvaluationCoFunction[EVENT, CTRL <: ServingMessage, OUT]
    extends CoProcessFunction[EVENT, CTRL, OUT]
    with CheckpointedFunction
    with LazyLogging {

  @transient
  private var snapshotMetadata: ListState[MetadataCheckpoint] = _

  @transient
  final protected var servingMetadata: immutable.Map[ModelId, ModelInfo] = _

  final protected lazy val servingModels: mutable.WeakHashMap[Int, PmmlModel] =
    mutable.WeakHashMap.empty[Int, PmmlModel]

  override def processElement2(control: CTRL,
                               ctx: CoProcessFunction[EVENT, CTRL, OUT]#Context,
                               out: Collector[OUT]): Unit = {
    manageModels(control)
    manageMetadata(control)
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    snapshotMetadata.clear()
    snapshotMetadata.add(new MetadataCheckpoint(servingMetadata.asJava))

  }

  override def initializeState(context: FunctionInitializationContext): Unit = {
    servingMetadata = immutable.Map.empty[ModelId, ModelInfo]

    val description = new ListStateDescriptor[MetadataCheckpoint](
      "metadata-snapshot",
      TypeInformation.of(new TypeHint[MetadataCheckpoint]() {}))

    snapshotMetadata = context.getOperatorStateStore.getUnionListState(description)

    if (context.isRestored) {
      Try(snapshotMetadata.get()) match {
        case Success(state) => servingMetadata ++= state.asScala.toSet[MetadataCheckpoint].flatMap(_.asScala).toMap
        case Failure(_) => logger.info("Not available state in ListState!")
      }
    }
  }

  final def loadModel(path: String): PmmlModel =
    Try(PmmlModel.fromReader(ModelReader(path))) match {
      case Success(model) =>
        logger.info("Model has been successfully loaded, model name: {}", model.modelName)
        model
      case Failure(e) => throw new ModelLoadingException(e.getMessage, e)
    }

  final def fromMetadata(modelId: String): PmmlModel = {
    val currentModelId: ModelId = ModelId.fromIdentifier(modelId)
    if (!servingMetadata.contains(currentModelId)) PmmlModel.empty
    else addAndRetrieveModel(modelId, currentModelId)
  }

  final private def addAndRetrieveModel(modelId: String, currentModelId: ModelId): PmmlModel = {
    val currentModelPath = servingMetadata(currentModelId).path
    val loadedModel = loadModel(currentModelPath)
    servingModels += (modelId.hashCode -> loadedModel)
    loadedModel
  }

  final private def manageMetadata(control: CTRL): Unit =
    control match {
      case add: AddMessage =>
        servingMetadata = MetadataManager(add, servingMetadata)
      case del: DelMessage =>
        servingMetadata = MetadataManager(del, servingMetadata)
    }

  final private def manageModels(control: CTRL): Unit =
    control match {
      case del: DelMessage => servingModels --= ModelsManager(del, servingModels)
      case _ =>
    }

}
