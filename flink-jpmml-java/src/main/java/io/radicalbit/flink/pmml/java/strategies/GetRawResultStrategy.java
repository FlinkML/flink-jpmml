package io.radicalbit.flink.pmml.java.strategies;

import io.radicalbit.flink.pmml.java.EvaluationResult;

import java.util.Map;

public class GetRawResultStrategy implements ResultExtractionStrategy {

    protected GetRawResultStrategy() {
    }

    @Override
    public Map<String, Object> extract(EvaluationResult result) {
        return result.getRawResult();
    }
}
