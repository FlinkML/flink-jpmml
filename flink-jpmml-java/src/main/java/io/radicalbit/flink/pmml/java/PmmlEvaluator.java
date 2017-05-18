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
