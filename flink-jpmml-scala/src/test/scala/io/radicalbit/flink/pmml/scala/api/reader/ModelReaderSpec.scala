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

import java.io.{File, IOException}

import org.apache.flink.core.fs.Path
import org.apache.flink.runtime.fs.hdfs.HadoopFileSystem
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileUtil
import org.apache.hadoop.hdfs.MiniDFSCluster
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.util.Try

object ModelReaderSpec {
  private case class FakeReader()

  private val modelString =
    """|<?xml version="1.0" encoding="UTF-8"?>
       |<PMML version="4.3" xmlns="http://www.dmg.org/PMML-4_3">
       |    <Header copyright="KNIME">
       |        <Application name="KNIME" version="2.8.0"/>
       |    </Header>
       |    <DataDictionary numberOfFields="5">
       |        <DataField name="sepal_length" optype="continuous" dataType="double">
       |            <Interval closure="closedClosed" leftMargin="4.3" rightMargin="7.9"/>
       |        </DataField>
       |        <DataField name="sepal_width" optype="continuous" dataType="double">
       |            <Interval closure="closedClosed" leftMargin="2.0" rightMargin="4.4"/>
       |        </DataField>
       |        <DataField name="petal_length" optype="continuous" dataType="double">
       |            <Interval closure="closedClosed" leftMargin="1.0" rightMargin="6.9"/>
       |        </DataField>
       |        <DataField name="petal_width" optype="continuous" dataType="double">
       |            <Interval closure="closedClosed" leftMargin="0.1" rightMargin="2.5"/>
       |        </DataField>
       |        <DataField name="clazz" optype="categorical" dataType="string">
       |            <Value value="Iris-setosa"/>
       |            <Value value="Iris-versicolor"/>
       |            <Value value="Iris-virginica"/>
       |        </DataField>
       |    </DataDictionary>
       |    <ClusteringModel modelName="k-means" functionName="clustering" modelClass="centerBased" numberOfClusters="4">
       |        <MiningSchema>
       |            <MiningField name="sepal_length" invalidValueTreatment="asIs"/>
       |            <MiningField name="sepal_width" invalidValueTreatment="asIs"/>
       |            <MiningField name="petal_length" invalidValueTreatment="asIs"/>
       |            <MiningField name="petal_width" invalidValueTreatment="asIs"/>
       |            <MiningField name="clazz" invalidValueTreatment="asIs" usageType="predicted"/>
       |        </MiningSchema>
       |        <ComparisonMeasure kind="distance">
       |            <squaredEuclidean/>
       |        </ComparisonMeasure>
       |        <ClusteringField field="sepal_length" compareFunction="absDiff"/>
       |        <ClusteringField field="sepal_width" compareFunction="absDiff"/>
       |        <ClusteringField field="petal_length" compareFunction="absDiff"/>
       |        <ClusteringField field="petal_width" compareFunction="absDiff"/>
       |        <Cluster name="cluster_0" size="32">
       |            <Array n="4" type="real">6.9125000000000005 3.099999999999999 5.846874999999999 2.1312499999999996</Array>
       |        </Cluster>
       |        <Cluster name="cluster_1" size="41">
       |            <Array n="4" type="real">6.23658536585366 2.8585365853658535 4.807317073170731 1.6219512195121943</Array>
       |        </Cluster>
       |        <Cluster name="cluster_2" size="50">
       |            <Array n="4" type="real">5.005999999999999 3.4180000000000006 1.464 0.2439999999999999</Array>
       |        </Cluster>
       |        <Cluster name="cluster_3" size="27">
       |            <Array n="4" type="real">5.529629629629629 2.6222222222222222 3.940740740740741 1.2185185185185188</Array>
       |        </Cluster>
       |        <Output>
       |            <OutputField name="PCluster" optype="clazz" dataType="integer" targetField="clazz" feature="entityId"/>
       |        </Output>
       |    </ClusteringModel>
       |
       |</PMML>
    """.stripMargin

}
class ModelReaderSpec extends WordSpec with BeforeAndAfter with Matchers {

  import ModelReaderSpec._

  protected var hdfsURI: String = _
  private var hdfsCluster: MiniDFSCluster = _
  private var hdfsPath: org.apache.hadoop.fs.Path = _
  protected var hdfs: org.apache.hadoop.fs.FileSystem = _

  before {
    try {
      val conf = new Configuration()
      val baseDir = new File("./target/hdfs/hdfsTestModel").getAbsoluteFile
      FileUtil.fullyDelete(baseDir)
      conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir.getAbsolutePath)

      val builder = new MiniDFSCluster.Builder(conf)
      hdfsCluster = builder.build()

      hdfsURI = "hdfs://" + hdfsCluster.getURI.getHost + ":" + hdfsCluster.getNameNodePort + "/"

      hdfsPath = new org.apache.hadoop.fs.Path("/model.xml")
      hdfs = hdfsPath.getFileSystem(conf)
      val stream = hdfs.create(hdfsPath)
      stream.write(modelString.getBytes("UTF-8"))
      stream.close()
    } catch {
      case (e: Throwable) =>
        e.printStackTrace()
        fail("Test fail" + e.getMessage)
    }
  }

  after {
    try {
      hdfs.delete(hdfsPath, false)
      hdfsCluster.shutdown()
    } catch {
      case (e: IOException) =>
        throw new RuntimeException(e)
    }
  }

  "Model reader on HDFS" should {

    "control GetFileSystem type" in {
      val pathFile = new Path(hdfsURI + hdfsPath)
      val fs = pathFile.getFileSystem
      fs shouldBe a[HadoopFileSystem]
    }

    "support correctly FsReader" in {
      val pathFile = hdfsURI + hdfsPath
      val reader = new ModelReader(pathFile) with FsReader
      val reuslt = Try(reader.buildDistributedPath)
      reuslt.getOrElse("") should equal(modelString)
    }

    "support FsReader on not found path model" in {
      val wrongPath = hdfsURI + "/wrong/path"
      val reader = new ModelReader(wrongPath) with FsReader
      val result = Try(reader.buildDistributedPath)
      result.getOrElse("") should equal("")
    }
  }
}
