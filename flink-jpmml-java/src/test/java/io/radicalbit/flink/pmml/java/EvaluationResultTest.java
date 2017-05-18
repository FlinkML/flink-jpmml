package io.radicalbit.flink.pmml.java;

import io.radicalbit.flink.pmml.java.strategies.MissingValueStrategies;
import io.radicalbit.flink.pmml.java.strategies.PreparationErrorStrategies;
import org.dmg.pmml.Model;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.clustering.ClusterAffinityDistribution;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EvaluationResultTest {
    EvaluationResult testResult;

    EvaluationResult testResultNoTargetNoOutput;

    public EvaluationResultTest() throws Exception {


    }

    @Before
    public void initResult() throws Exception {
        ModelEvaluator<? extends Model> evaluator = PMMLTestUtils.irisEvaluator();
        testResult = new RawInput(PMMLTestUtils.simpleIrisInput()).prepare(evaluator,
                PreparationErrorStrategies.propagateExceptionStrategy(),
                MissingValueStrategies.propagateExceptionStrategy()).evaluate();

        ModelEvaluator<? extends Model> evaluatorNoTargetNoOutput = PMMLTestUtils.irisEvaluatorNoTargetNoOutput();

        testResultNoTargetNoOutput = new RawInput(PMMLTestUtils.simpleIrisInput()).prepare(evaluatorNoTargetNoOutput,
                PreparationErrorStrategies.propagateExceptionStrategy(),
                MissingValueStrategies.propagateExceptionStrategy()).evaluate();
    }

    @Test
    public void testGetOutputField() {
        Map<String, Object> result = testResult.getOutputFields();

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("PCluster", 3);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetTargetField() {
        Map<String, Object> result = testResult.getTargetFields();

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("clazz", "3");
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetTargetAndOutputField() {
        Map<String, Object> result = testResult.getTargetAndOutputFields();

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("clazz", "3");
        expectedResult.put("PCluster", 3);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetTargetAndOutputFieldWithNoTargetOrOutputFieldSpecified() {
        Map<String, Object> result = testResultNoTargetNoOutput.getTargetAndOutputFields();

        assert (result.isEmpty());

    }

    @Test
    public void testGetRawResultNoTargetNoOutput() {
        Map<String, Object> result = testResultNoTargetNoOutput.getRawResult();

        assertEquals(result.size(), 1);
        assert (result.containsKey(""));
        assert (result.get("") instanceof ClusterAffinityDistribution);
    }

    @Test
    public void testGetRawResult() {
        Map<String, Object> result = testResult.getRawResult();

        assertEquals(result.size(), 2);
        assertEquals(result.get("PCluster"), 3);

        assert (result.get("clazz") instanceof ClusterAffinityDistribution);
    }

}
