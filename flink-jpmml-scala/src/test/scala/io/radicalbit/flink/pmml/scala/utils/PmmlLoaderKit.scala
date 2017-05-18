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

  final protected def getPMMLSource(path: String): String = {
    getClass.getResource(path).getPath
  }

  final protected def getPMMLResource(path: String): PMML = {
    val source = scala.io.Source.fromURL(getClass.getResource(path)).reader()
    JAXBUtil.unmarshalPMML(ImportFilter.apply(new InputSource(source)))
  }

}
