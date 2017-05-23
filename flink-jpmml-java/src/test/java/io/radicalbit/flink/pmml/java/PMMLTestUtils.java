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

package io.radicalbit.flink.pmml.java;

import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.JAXBUtil;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class PMMLTestUtils {


    public static ModelEvaluator<? extends Model> irisEvaluator() throws IOException, SAXException, JAXBException {

        return ModelEvaluatorFactory.newInstance().newModelEvaluator(irisPMML());

    }

    public static PMML irisPMML() throws IOException, SAXException, JAXBException {


        return JAXBUtil.unmarshalPMML(
                new StreamSource(new StringReader(irisPMMLSource())));
    }

    public static String irisPMMLSource() throws IOException {
        return org.apache.commons.io.IOUtils.toString(
                new FileInputStream(
                        PMMLTestUtils.class.getClassLoader().getResource("single_iris_kmeans.xml").getFile()));

    }

    public static String irisPMML42Source() throws IOException {
        return org.apache.commons.io.IOUtils.toString(
                new FileInputStream(
                        PMMLTestUtils.class.getClassLoader().getResource("single_iris_kmeans_42.xml").getFile()));

    }

    public static String irisPMML41Source() throws IOException {
        return org.apache.commons.io.IOUtils.toString(
                new FileInputStream(
                        PMMLTestUtils.class.getClassLoader().getResource("single_iris_kmeans_41.xml").getFile()));

    }

    public static String irisPMML40Source() throws IOException {
        return org.apache.commons.io.IOUtils.toString(
                new FileInputStream(
                        PMMLTestUtils.class.getClassLoader().getResource("single_iris_kmeans_40.xml").getFile()));

    }

    public static String irisPMML32Source() throws IOException {
        return org.apache.commons.io.IOUtils.toString(
                new FileInputStream(
                        PMMLTestUtils.class.getClassLoader().getResource("single_iris_kmeans_32.xml").getFile()));

    }

    public static ModelEvaluator<? extends Model> irisEvaluatorNoTargetNoOutput() throws IOException, SAXException, JAXBException {

        return ModelEvaluatorFactory.newInstance().newModelEvaluator(irisPMMLNoTargetNoOutput());

    }

    public static PMML irisPMMLNoTargetNoOutput() throws IOException, SAXException, JAXBException {


        return JAXBUtil.unmarshalPMML(
                new StreamSource(new StringReader(irisPMMLSourceNoTargetNoOutput())));
    }

    public static String irisPMMLSourceNoTargetNoOutput() throws IOException {
        return org.apache.commons.io.IOUtils.toString(
                new FileInputStream(
                        PMMLTestUtils.class.getClassLoader().getResource("single_iris_kmeans_no_output_no_target.xml").getFile()));

    }

    public static Map<String, Object> expectedEvaluationOutput() {
        Map<String, Object> expectedResult = new HashMap<String, Object>();
        expectedResult.put("PCluster", 3);
        expectedResult.put("clazz", "3");

        return expectedResult;
    }

    public static Map<String, Object> simpleIrisInput() {
        Map<String, Object> m = new HashMap<>();
        m.put("sepal_width", 1.0);
        m.put("sepal_length", 1.0);
        m.put("petal_width", 1.0);
        m.put("petal_length", 1.0);
        return m;
    }
}
