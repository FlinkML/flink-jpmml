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

package io.radicalbit.flink.pmml.java.api;

import io.radicalbit.flink.pmml.java.EvaluationResult;
import io.radicalbit.flink.pmml.java.PreparedInput;
import io.radicalbit.flink.pmml.java.RawInput;
import io.radicalbit.flink.pmml.java.strategies.MissingValueStrategy;
import io.radicalbit.flink.pmml.java.strategies.PreparationErrorStrategy;
import io.radicalbit.flink.pmml.java.strategies.ResultExtractionStrategy;
import org.dmg.pmml.Model;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.util.Map;

public class JPMMLEvaluationOperatorUtil {

    /**
     * Default processing logic. Used by JPMMLEvaluationFlatMapOperator and
     * JPMMLEvaluationMapOperator.
     *
     * @param input
     * @param preparationErrorStrategy
     * @param missingValueStrategy
     * @param evaluator
     * @param resultExtractionStrategy
     * @return
     * @throws Exception
     */

    public static Map<String, Object> process(Map<String, Object> input,
                                              PreparationErrorStrategy preparationErrorStrategy,
                                              MissingValueStrategy missingValueStrategy,
                                              ModelEvaluator<? extends Model> evaluator,
                                              ResultExtractionStrategy resultExtractionStrategy
    ) throws Exception {

        final RawInput i = new RawInput(input);
        final PreparedInput pi = i.prepare(evaluator, preparationErrorStrategy, missingValueStrategy);
        final EvaluationResult result = pi.evaluate();
        return resultExtractionStrategy.extract(result);
    }

    public static ModelEvaluator<? extends Model> unmarshallPMML(String pmmlSource) throws JAXBException, SAXException {
        final ModelEvaluatorFactory factory = ModelEvaluatorFactory.newInstance();
        return factory.newModelEvaluator(JAXBUtil.unmarshalPMML(fromFilteredSource(pmmlSource)));
    }

    private static SAXSource fromFilteredSource(String sourcePath) throws SAXException {
        return JAXBUtil.createFilteredSource(new InputSource(new StringReader(sourcePath)), new ImportFilter());
    }
}
