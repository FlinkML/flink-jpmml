package io.radicalbit.flink.pmml.java;

import io.radicalbit.flink.pmml.java.strategies.MissingValueStrategies;
import io.radicalbit.flink.pmml.java.strategies.PreparationErrorStrategies;
import org.dmg.pmml.FieldName;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PreparedInputTest {

    @Test
    public void testInputEvaluation() throws Exception {
        PreparedInput input = new RawInput(PMMLTestUtils.simpleIrisInput())
                .prepare(PMMLTestUtils.irisEvaluator(), PreparationErrorStrategies.propagateExceptionStrategy(),
                        MissingValueStrategies.propagateExceptionStrategy()
                );
        EvaluationResult result = input.evaluate();

        assertTrue(result.getResultData().containsKey(new FieldName("clazz")));
    }
}
