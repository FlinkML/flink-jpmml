package io.radicalbit.flink.pmml.java.strategies;

import io.radicalbit.flink.pmml.java.EvaluationResult;

import java.util.Map;

/**
 * Simple extraction strategy that returns a Map containing the target and output fields specified in the PMML file.
 */

final public class ExtractTargetAndOutputFieldStrategy implements ResultExtractionStrategy {

    protected ExtractTargetAndOutputFieldStrategy() {
    }

    @Override
    final public Map<String, Object> extract(EvaluationResult rawResult) {
        return rawResult.getTargetAndOutputFields();
    }
}