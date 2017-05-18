package io.radicalbit.flink.pmml.java.strategies;

import io.radicalbit.flink.pmml.java.EvaluationResult;

import java.io.Serializable;
import java.util.Map;


/**
 * Strategy to extract values from an EvaluationResult.
 */
public interface ResultExtractionStrategy extends Serializable {
    Map<String, Object> extract(EvaluationResult result);


}
