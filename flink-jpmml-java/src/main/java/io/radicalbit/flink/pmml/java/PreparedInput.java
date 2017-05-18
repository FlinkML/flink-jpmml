package io.radicalbit.flink.pmml.java;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;

import java.util.Map;

/**
 * Data class wrapping the result of an input preparation.
 */
public class PreparedInput {
    private final Map<FieldName, FieldValue> preparedData;
    private final Evaluator evaluator;

    public PreparedInput(Map<FieldName, FieldValue> data, Evaluator evaluator_t) {
        preparedData = data;
        evaluator = evaluator_t;

    }

    public final EvaluationResult evaluate() {
        final Map<FieldName, Object> result = (Map<FieldName, Object>) evaluator.evaluate(this.preparedData);
        return new EvaluationResult(result, evaluator);
    }

    public Map<FieldName, FieldValue> getPreparedData() {
        return preparedData;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

}
