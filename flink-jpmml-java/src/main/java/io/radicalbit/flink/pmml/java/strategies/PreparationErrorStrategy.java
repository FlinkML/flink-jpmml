package io.radicalbit.flink.pmml.java.strategies;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;

import java.io.Serializable;

/**
 * Strategy to handle errors raised during input preparation.
 */
public interface PreparationErrorStrategy extends Serializable {
    FieldValue handleError(FieldName name, Object rawValue, Evaluator evaluator, Exception e) throws Exception;


}
