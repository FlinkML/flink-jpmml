package io.radicalbit.flink.pmml.java.api;

import io.radicalbit.flink.pmml.java.strategies.*;

import javax.xml.bind.JAXBException;

/**
 * Builder class to create JPMMLEvaluationMapOperator and JPMMLEvaluationFlatMapOperator.
 */

public class JPMMLEvaluationOperatorBuilder {

    private ExceptionHandlingStrategy exceptionHandlingStrategy = ExceptionHandlingStrategies.logExceptionStrategy();
    private PreparationErrorStrategy preparationErrorStrategy = PreparationErrorStrategies.propagateExceptionStrategy();
    private ResultExtractionStrategy resultExtractionStrategy = ResultExtractionStrategies.extractTargetAndOutputFieldStrategy();
    private MissingValueStrategy missingValueStrategy = MissingValueStrategies.propagateExceptionStrategy();

    private String pmmlSource;

    /**
     * Factory method to create a builder from a Evaluator.
     *
     * @param pmmlSource String containing the PMML specification of a model.
     * @return
     */
    public static JPMMLEvaluationOperatorBuilder create(String pmmlSource) {
        return new JPMMLEvaluationOperatorBuilder(pmmlSource);
    }

    private JPMMLEvaluationOperatorBuilder(String pmmlSource_t) {
        pmmlSource = pmmlSource_t;
    }

    public JPMMLEvaluationMapOperator buildMapOperator() throws JAXBException {
        return new JPMMLEvaluationMapOperator(
                pmmlSource,
                preparationErrorStrategy,
                resultExtractionStrategy,
                missingValueStrategy
        );
    }

    public JPMMLEvaluationFlatMapOperator buildFlatMapOperator() throws JAXBException {
        return new JPMMLEvaluationFlatMapOperator(
                pmmlSource,
                exceptionHandlingStrategy,
                preparationErrorStrategy,
                resultExtractionStrategy,
                missingValueStrategy
        );
    }

    public JPMMLEvaluationOperatorBuilder setExceptionHandlingStrategy(ExceptionHandlingStrategy exceptionHandlingStrategy) {
        this.exceptionHandlingStrategy = exceptionHandlingStrategy;
        return this;
    }

    public JPMMLEvaluationOperatorBuilder setPreparationErrorStrategy(PreparationErrorStrategy preparationErrorStrategy) {
        this.preparationErrorStrategy = preparationErrorStrategy;
        return this;
    }

    public JPMMLEvaluationOperatorBuilder setResultExtractionStrategy(ResultExtractionStrategy resultExtractionStrategy) {
        this.resultExtractionStrategy = resultExtractionStrategy;
        return this;
    }

    public JPMMLEvaluationOperatorBuilder setMissingValueStrategy(MissingValueStrategy missingValueStrategy) {
        this.missingValueStrategy = missingValueStrategy;
        return this;
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

    public String getPmmlSource() {
        return pmmlSource;
    }

    public void setPmmlSource(String pmmlSource) {
        this.pmmlSource = pmmlSource;
    }
}
