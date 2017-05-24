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


import io.radicalbit.flink.pmml.java.api.JPMMLEvaluationFlatMapOperator;
import io.radicalbit.flink.pmml.java.api.JPMMLEvaluationOperatorBuilder;
import io.radicalbit.flink.pmml.java.strategies.*;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.dmg.pmml.DataType;

import javax.xml.bind.JAXBException;
import java.util.Map;

public class PmmlEvaluator {

    public PmmlEvaluator(String sourcePath) {
        pmmlSource = sourcePath;
    }

    private final String pmmlSource;

    private PreparationErrorStrategy inputPreparationStrategy = PreparationErrorStrategies.propagateExceptionStrategy();

    private MissingValueStrategy missingValueStrategy = MissingValueStrategies.delegateToPMMLStrategy();

    private ResultExtractionStrategy resultExtractionStrategy = ResultExtractionStrategies.getRawResultStrategy();

    private ExceptionHandlingStrategy exceptionHandlingStrategy = ExceptionHandlingStrategies.logExceptionStrategy();

    // ====================== Input Preparation Strategy setters =======================

    public PmmlEvaluator propagateInputPreparationException() {
        inputPreparationStrategy = PreparationErrorStrategies.propagateExceptionStrategy();
        return this;
    }

    // ====================== Missing Values Strategy setters =======================

    public PmmlEvaluator delegateMissingValuesToPmml() {
        missingValueStrategy = MissingValueStrategies.delegateToPMMLStrategy();
        return this;
    }

    public PmmlEvaluator replaceMissingValuesWithDefault(Map<DataType, Object> defaultValue) {
        missingValueStrategy = MissingValueStrategies.defaultValueStrategy(defaultValue);
        return this;
    }

    // ====================== Result Extraction Strategy setters =======================

    public PmmlEvaluator extractRawResults() {
        resultExtractionStrategy = ResultExtractionStrategies.getRawResultStrategy();
        return this;
    }

    public PmmlEvaluator extractOutputAndTargetFields() {
        resultExtractionStrategy = ResultExtractionStrategies.extractTargetAndOutputFieldStrategy();
        return this;
    }

    // ====================== Result Exception Handling Strategy setters =======================

    public PmmlEvaluator logExceptions() {
        exceptionHandlingStrategy = ExceptionHandlingStrategies.logExceptionStrategy();
        return this;
    }

    public DataStream<Map<String, Object>> predict(DataStream<Map<String, Object>> input) throws JAXBException {
        JPMMLEvaluationOperatorBuilder predictorBuilder = JPMMLEvaluationOperatorBuilder
                .create(pmmlSource)
                .setPreparationErrorStrategy(inputPreparationStrategy)
                .setMissingValueStrategy(missingValueStrategy)
                .setResultExtractionStrategy(resultExtractionStrategy)
                .setExceptionHandlingStrategy(exceptionHandlingStrategy);

        JPMMLEvaluationFlatMapOperator predictor = predictorBuilder.buildFlatMapOperator();

        return input.flatMap(predictor);
    }

}
