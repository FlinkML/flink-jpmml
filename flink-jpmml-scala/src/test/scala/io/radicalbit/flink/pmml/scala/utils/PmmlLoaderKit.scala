/*
 *
 * Copyright (c) 2017 Radicalbit
 *
 * This file is part of flink-JPMML
 *
 * flink-JPMML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * flink-JPMML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with flink-JPMML.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package io.radicalbit.flink.pmml.scala.utils

import org.dmg.pmml.PMML
import org.jpmml.model.{ImportFilter, JAXBUtil}
import org.xml.sax.InputSource

trait PmmlLoaderKit {

  protected case object Source {
    val KmeansPmml = "/kmeans.xml"
    val KmeansPmml41 = "/kmeans41.xml"
    val KmeansPmml40 = "/kmeans40.xml"
    val KmeansPmml42 = "/kmeans42.xml"
    val KmeansPmml32 = "/kmeans41.xml"

    val KmeansPmmlEmpty = "/kmeans_empty.xml"
    val KmeansPmmlNoOut = "/kmeans_nooutput.xml"
    val KmeansPmmlStringFields = "/kmeans_stringfields.xml"
    val KmeansPmmlNoOutNoTrg = "/kmeans_nooutput_notarget.xml"
    val NotExistingPath: String = "/not/existing/" + scala.util.Random.nextString(4)
  }

  final protected def getPMMLSource(path: String): String =
    getClass.getResource(path).getPath

  final protected def getPMMLResource(path: String): PMML = {
    val source = scala.io.Source.fromURL(getClass.getResource(path)).reader()
    JAXBUtil.unmarshalPMML(ImportFilter.apply(new InputSource(source)))
  }

}
