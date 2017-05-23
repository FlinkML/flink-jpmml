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

import io.radicalbit.flink.pmml.java.strategies.*;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.ModelEvaluator;

import javax.xml.bind.JAXBException;
import java.util.Map;

/**
 * JPMML operator to be used with a flatMap call.
 * This function implements the standard logic of evaluation: input validation -> evaluation ->result processing.
 * If at any step the process raise an exception, the exception is handled by the dedicated function and the result
 * is not collected.
 */

public final class JPMMLEvaluationFlatMapOperator
        extends RichFlatMapFunction<Map<String, Object>, Map<String, Object>> {
    private final ExceptionHandlingStrategy exceptionHandlingStrategy;
    private final PreparationErrorStrategy preparationErrorStrategy;
    private final ResultExtractionStrategy resultExtractionStrategy;
    private final MissingValueStrategy missingValueStrategy;

    //The evaluator is transient and built in the open() function to allow proper serializability of the operator.
    private transient ModelEvaluator evaluator;
    private final String pmmlSource;

    protected JPMMLEvaluationFlatMapOperator(String pmmlSource_t,
                                             ExceptionHandlingStrategy e,
                                             PreparationErrorStrategy p,
                                             ResultExtractionStrategy r,
                                             MissingValueStrategy m
    ) throws JAXBException {

        pmmlSource = pmmlSource_t;
        exceptionHandlingStrategy = e;
        preparationErrorStrategy = p;
        resultExtractionStrategy = r;
        missingValueStrategy = m;
    }

    protected JPMMLEvaluationFlatMapOperator(String pmmlSource) throws JAXBException {
        this(pmmlSource,
                ExceptionHandlingStrategies.logExceptionStrategy(),
                PreparationErrorStrategies.propagateExceptionStrategy(),
                ResultExtractionStrategies.extractTargetAndOutputFieldStrategy(),
                MissingValueStrategies.propagateExceptionStrategy());
    }


    @Override
    public void flatMap(Map<String, Object> input, Collector<Map<String, Object>> collector) throws Exception {
        try {
            collector.collect(
                    JPMMLEvaluationOperatorUtil.process(input,
                            preparationErrorStrategy,
                            missingValueStrategy,
                            evaluator,
                            resultExtractionStrategy
                    ));
        } catch (Exception e) {
            exceptionHandlingStrategy.handleException(e);
        }
    }

    public ExceptionHandlingStrategy getExceptionHandlingStrategy() {
        return exceptionHandlingStrategy;
    }

    public PreparationErrorStrategy getPreparationErrorStrategy() {
        return preparationErrorStrategy;
    }

    public ResultExtractionStrategy getResultExtractionStrategy() {
        return resultExtractionStrategy;
    }

    public MissingValueStrategy getMissingValueStrategy() {
        return missingValueStrategy;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        this.evaluator = JPMMLEvaluationOperatorUtil.unmarshallPMML(pmmlSource);
    }
}
