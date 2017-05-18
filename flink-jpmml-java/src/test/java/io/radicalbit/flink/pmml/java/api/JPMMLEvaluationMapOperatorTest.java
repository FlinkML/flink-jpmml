package io.radicalbit.flink.pmml.java.api;

import io.radicalbit.flink.pmml.java.PMMLTestUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class JPMMLEvaluationMapOperatorTest {

    public JPMMLEvaluationMapOperatorTest() {
    }

    @Test
    public void testMapSuccessful() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        String pmml = PMMLTestUtils.irisPMMLSource();
        JPMMLEvaluationMapOperator operator = new JPMMLEvaluationMapOperator(pmml);

        List<Map<String, Object>> input = new LinkedList<>();
        input.add(PMMLTestUtils.simpleIrisInput());
        DataSource<Map<String, Object>> dataset = env.fromCollection(input);

        List<Map<String, Object>> collectedResult =
                dataset.map((MapFunction) operator).returns(java.util.Map.class).collect();

        assertTrue(collectedResult.get(0).equals(PMMLTestUtils.expectedEvaluationOutput()));

    }

    @Test
    public void testOld42Version() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        String pmml = PMMLTestUtils.irisPMML42Source();
        JPMMLEvaluationMapOperator operator = new JPMMLEvaluationMapOperator(pmml);

        List<Map<String, Object>> input = new LinkedList<>();
        input.add(PMMLTestUtils.simpleIrisInput());
        DataSource<Map<String, Object>> dataset = env.fromCollection(input);

        List<Map<String, Object>> collectedResult =
                dataset.map((MapFunction) operator).returns(java.util.Map.class).collect();

        assertTrue(collectedResult.get(0).equals(PMMLTestUtils.expectedEvaluationOutput()));
    }

    @Test
    public void testOld41Version() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        String pmml = PMMLTestUtils.irisPMML41Source();
        JPMMLEvaluationMapOperator operator = new JPMMLEvaluationMapOperator(pmml);

        List<Map<String, Object>> input = new LinkedList<>();
        input.add(PMMLTestUtils.simpleIrisInput());
        DataSource<Map<String, Object>> dataset = env.fromCollection(input);

        List<Map<String, Object>> collectedResult =
                dataset.map((MapFunction) operator).returns(java.util.Map.class).collect();

        assertTrue(collectedResult.get(0).equals(PMMLTestUtils.expectedEvaluationOutput()));
    }

    @Test
    public void testOld40Version() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        String pmml = PMMLTestUtils.irisPMML40Source();
        JPMMLEvaluationMapOperator operator = new JPMMLEvaluationMapOperator(pmml);

        List<Map<String, Object>> input = new LinkedList<>();
        input.add(PMMLTestUtils.simpleIrisInput());
        DataSource<Map<String, Object>> dataset = env.fromCollection(input);

        List<Map<String, Object>> collectedResult =
                dataset.map((MapFunction) operator).returns(java.util.Map.class).collect();

        assertTrue(collectedResult.get(0).equals(PMMLTestUtils.expectedEvaluationOutput()));
    }

    @Test
    public void testOld32Version() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        String pmml = PMMLTestUtils.irisPMML32Source();
        JPMMLEvaluationMapOperator operator = new JPMMLEvaluationMapOperator(pmml);

        List<Map<String, Object>> input = new LinkedList<>();
        input.add(PMMLTestUtils.simpleIrisInput());
        DataSource<Map<String, Object>> dataset = env.fromCollection(input);

        List<Map<String, Object>> collectedResult = dataset
                .map((MapFunction) operator).returns(java.util.Map.class).collect();

        assertTrue(collectedResult.get(0).equals(PMMLTestUtils.expectedEvaluationOutput()));
    }
}