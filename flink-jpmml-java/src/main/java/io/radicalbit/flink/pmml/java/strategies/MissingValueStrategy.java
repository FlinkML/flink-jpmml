package io.radicalbit.flink.pmml.java.strategies;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.ModelEvaluator;

import java.io.Serializable;

/**
 * Strategy to be used to handle missing values in a raw input.
 */
public interface MissingValueStrategy extends Serializable {
    Object handleError(FieldName fieldName, ModelEvaluator evaluator) throws Exception;

}


