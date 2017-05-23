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
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.dmg.pmml.Model;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.ModelEvaluator;

import javax.xml.bind.JAXBException;
import java.util.Map;

/**
 * JPMML operator to be used with a map call.
 * This function implements the standard logic of evaluation: input validation -> evaluation ->result processing.
 * If the computation fails at any step, the operator will raise an exception.
 */

public final class JPMMLEvaluationMapOperator extends RichMapFunction<Map<String, Object>, Map<String, Object>> {

    private final PreparationErrorStrategy preparationErrorStrategy;
    private final ResultExtractionStrategy resultExtractionStrategy;

    //The evaluator is transient and built in the open() function to allow proper serializability of the operator.
    private transient ModelEvaluator<? extends Model> evaluator;
    private final String pmmlSource;

    private final MissingValueStrategy missingValueStrategy;

    protected JPMMLEvaluationMapOperator(String pmmlSource_t, PreparationErrorStrategy p, ResultExtractionStrategy r, MissingValueStrategy m) throws JAXBException {
        pmmlSource = pmmlSource_t;
        evaluator = null;
        preparationErrorStrategy = p;
        resultExtractionStrategy = r;
        missingValueStrategy = m;
    }

    protected JPMMLEvaluationMapOperator(String pmmlSource) throws JAXBException {
        this(pmmlSource,
                PreparationErrorStrategies.propagateExceptionStrategy(),
                ResultExtractionStrategies.extractTargetAndOutputFieldStrategy(),
                MissingValueStrategies.propagateExceptionStrategy()
        );
    }


    @Override
    public final Map<String, Object> map(Map<String, Object> input) throws Exception {
        return JPMMLEvaluationOperatorUtil.process(input,
                preparationErrorStrategy,
                missingValueStrategy,
                evaluator,
                resultExtractionStrategy
        );
    }

    public MissingValueStrategy getMissingValueStrategy() {
        return missingValueStrategy;
    }

    public PreparationErrorStrategy getPreparationErrorStrategy() {
        return preparationErrorStrategy;
    }

    public ResultExtractionStrategy getResultExtractionStrategy() {
        return resultExtractionStrategy;
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
