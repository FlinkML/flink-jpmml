package io.radicalbit.flink.pmml.java.strategies;

import io.radicalbit.flink.pmml.java.InputPreparationException;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.ModelEvaluator;


/**
 * This strategy just re-throws the exception raised underneath.
 */
public final class PropagateExceptionStrategy implements MissingValueStrategy, PreparationErrorStrategy {

    public PropagateExceptionStrategy() {
    }

    @Override
    public final Object handleError(FieldName fieldName, ModelEvaluator evaluator) throws Exception {
        throw new InputPreparationException("Field named " + fieldName.getValue() + " is missing.");
    }

    @Override
    public FieldValue handleError(FieldName name, Object rawValue, Evaluator evaluator, Exception e) throws Exception {
        throw e;
    }
}
