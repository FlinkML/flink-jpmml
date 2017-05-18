package io.radicalbit.flink.pmml.java.api;

import io.radicalbit.flink.pmml.java.PMMLTestUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.dmg.pmml.FieldName;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class JPMMLEvaluationFlatMapOperatorTest {

    public JPMMLEvaluationFlatMapOperatorTest() {
    }

    @Test
    public void testFlatMapSuccessful() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        String pmml = PMMLTestUtils.irisPMMLSource();
        JPMMLEvaluationFlatMapOperator operator = new JPMMLEvaluationFlatMapOperator(pmml);

        List<Map<String, Object>> input = new LinkedList<>();
        input.add(PMMLTestUtils.simpleIrisInput());
        DataSource<Map<String, Object>> dataset = env.fromCollection(input);
        Map<FieldName, Object> c = new HashMap<FieldName, Object>();
        List<Map<String, Object>> collectedResult = dataset.flatMap((FlatMapFunction) operator).returns(Map.class).collect();

        Map<String, Object> expectedResult = new HashMap<String, Object>();
        expectedResult.put("PCluster", 3);
        expectedResult.put("clazz", "3");
        assertTrue(collectedResult.get(0).equals(expectedResult));
    }

    @Test
    public void testFlatMapInvalidInput() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        String pmml = PMMLTestUtils.irisPMMLSource();
        JPMMLEvaluationFlatMapOperator operator = new JPMMLEvaluationFlatMapOperator(pmml);

        List<Map<String, Object>> input = new LinkedList<>();
        input.add(PMMLTestUtils.simpleIrisInput());
        Map<String, Object> wrongInput = PMMLTestUtils.simpleIrisInput();
        wrongInput.remove("sepal_length");
        input.add(wrongInput);
        DataSource<Map<String, Object>> dataset = env.fromCollection(input);
        Map<FieldName, Object> c = new HashMap<FieldName, Object>();
        List<Map<String, Object>> collectedResult = dataset.flatMap((FlatMapFunction) operator).returns(Map.class).collect();

        List<Map<String, Object>> expectedResult = new LinkedList();

        Map<String, Object> expectedResult1 = new HashMap<String, Object>();
        expectedResult1.put("PCluster", 3);
        expectedResult1.put("clazz", "3");

        expectedResult.add(expectedResult1);

        assertTrue(collectedResult.size() == 1);
        assertTrue(collectedResult.equals(expectedResult));

    }

}