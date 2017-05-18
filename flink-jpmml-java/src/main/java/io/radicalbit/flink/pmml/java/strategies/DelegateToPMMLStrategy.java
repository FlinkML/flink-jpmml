package io.radicalbit.flink.pmml.java.strategies;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.ModelEvaluator;


/**
 * This strategy does not handle a missing value and returns a null to let JPMML take care of the missing value according
 * to the policies specified in the PMML file.
 */
public class DelegateToPMMLStrategy implements MissingValueStrategy {
    @Override
    public Object handleError(FieldName fieldName, ModelEvaluator evaluator) throws Exception {
        return null;
    }
}
